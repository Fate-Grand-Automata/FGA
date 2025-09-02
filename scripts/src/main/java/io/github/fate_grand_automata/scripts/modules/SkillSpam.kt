package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.enums.NpGaugeEnum
import io.github.fate_grand_automata.scripts.enums.SpamEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.scripts.models.SkillSpamConfig
import io.github.fate_grand_automata.scripts.models.SkillSpamConfig.Companion.getParsedAction
import io.github.fate_grand_automata.scripts.models.SpamConfigPerTeamSlot
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.fate_grand_automata.scripts.models.skills
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class SkillSpam @Inject constructor(
    api: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val spamConfig: SpamConfigPerTeamSlot,
    private val caster: Caster
) : IFgoAutomataApi by api {
    companion object {
        val skillSpamDelay = 0.25.seconds

        // Skill cooldown detection (brightness of the bottom edge of the skill frame)
        private const val SKILL_COOLDOWN_BRIGHTNESS_THRESH = 102

        // --- HSV detection notes (summary) ---
        // NP gauge detection uses both Saturation (S) and Value (V)
        // to stay stable even with bright backgrounds or low-contrast cases.
        private const val VALUE_THRESH = 60.0
        private const val SATURATION_THRESH = 100.0

        /*
         * --- Detailed testing notes ---
         *
         * NP gauge:
         * - S only → misdetection (e.g., NP 99%: high S / low V)
         * - V only → misdetection (e.g., bright background: high V / low S)
         * - Brightness (gray) alone is unstable due to blinking/gradients
         * → Best stability with S && V
         *
         * Strict boundaries (no margin):
         * - NP charged   H: 11..22, S: 160..255, V: 74..238, gray: 46..170
         * - NP low       H: 0..125, S: 20..255,  V: 2..40,   gray: 1..28
         *
         * Skill cooldown:
         * - on cooldown  H: 13..16, S: 39..45, V: 72..85,   gray: 68..81
         * - ready        H: 0..32,  S: 0..60,  V: 188..255, gray: 179..255
         *
         * Tested stages: Fuyuki, Summer Event Beach
         */
    }

    fun spamSkills() {
        // Convert all skills into SkillEntry objects and sort by priority
        val sortedSkills = FieldSlot.list.flatMap { servantSlot ->
            val teamSlot = servantTracker.deployed[servantSlot] ?: return@flatMap emptyList()
            val servantSpamConfig = spamConfig[teamSlot]

            servantSpamConfig.skills.mapIndexedNotNull { index, skillConfig ->
                when (skillConfig.spam) {
                    SpamEnum.None -> null
                    else -> SkillEntry(teamSlot, servantSlot, index, skillConfig)
                }
            }
        }.sortedBy { it.skillConfig.priority }.toMutableList()

        var i = 0
        while (i < sortedSkills.size) {
            val entry = sortedSkills[i]
            val skill = entry.servantSlot.skills()[entry.skillIndex]
            val targets = entry.skillConfig.determineTargets(entry.servantSlot)
            val targetSlot = entry.skillConfig.determineActualTargetSlot(entry.servantSlot)
            var npCharged = false
            var isUsable = false

            if (caster.canSpam(entry.skillConfig.spam) && (state.stage + 1) in entry.skillConfig.waves) {
                var repeats = 0
                while (repeats < entry.skillConfig.maxRepeatCount) {
                    // Some delay for skill icon to be loaded
                    skillSpamDelay.wait()
                    // use color snapshot
                    useColor {
                        useSameSnapIn {
                            isUsable = isSkillReady(skill)

                            // Skip if the skill is on cooldown or the servant is in Eternal Sleep.
                            if (!isUsable || caster.isSkillIgnored(entry.teamSlot, entry.skillIndex)) {
                                isUsable = false
                                return@useSameSnapIn
                            }

                            if (hasNpChargedRelevant(entry)) {
                                npCharged = targetSlot.isNpCharged()
                            }
                        }
                    }

                    if (!isUsable) {
                        break
                    }

                    if (entry.skillConfig.canSpamFinal(npCharged)) {
                        caster.castServantSkill(skill, targets)
                    }
                    repeats++
                }
            }
            i++
        }
    }

    private fun isSkillReady(skill: Skill.Servant): Boolean =
        locations.battle.skillCooldownCheckRegion(skill)
            .isBrightnessAbove(SKILL_COOLDOWN_BRIGHTNESS_THRESH.toDouble())

    private fun hasNpChargedRelevant(entry: SkillEntry): Boolean {
        return when (entry.skillConfig.np) {
            NpGaugeEnum.Low, NpGaugeEnum.Ready -> true
            else -> false
        }
    }

    // A simple container for a skill, used for sorting and tracking
    private data class SkillEntry(
        val teamSlot: TeamSlot,
        val servantSlot: FieldSlot,
        val skillIndex: Int,
        val skillConfig: SkillSpamConfig
    )

    /**
     * Get skill target from a parsed auto-skill action
     */
    private fun SkillSpamConfig.determineActualTargetSlot(selfSlot: FieldSlot): FieldSlot {
        val tempTarget = getParsedAction(act)

        var actualSlot = tempTarget?.let {
            if (it.targets.isEmpty()) selfSlot

            when (it.targets.last()) {
                ServantTarget.A -> FieldSlot.A
                ServantTarget.B -> FieldSlot.B
                ServantTarget.C -> FieldSlot.C
                else -> selfSlot
            }
        } ?: selfSlot

        if (actualSlot != selfSlot) {
            val deployed = servantTracker.deployed

            // TODO: NpType3 reuses ServantTarget.ABC, so it's affected by deployed state. It should be independent of servant count.
            actualSlot = when (deployed.size) {
                1 -> FieldSlot.B
                2 -> {
                    if (actualSlot == FieldSlot.B) {
                        when (null) {
                            deployed[FieldSlot.A] -> FieldSlot.A
                            deployed[FieldSlot.C] -> FieldSlot.C
                            else -> FieldSlot.A // Assume Left when Slot B is empty
                        }
                    } else actualSlot
                }
                else -> actualSlot
            }
        }
        return actualSlot
    }

    /**
     * Get click targets from a parsed Auto-skill action
     */
    private fun SkillSpamConfig.determineTargets(fieldSlot: FieldSlot): List<ServantTarget> {
        val tempTarget =  getParsedAction(act)
        return when {
            tempTarget == null || tempTarget.targets.isEmpty() -> {
                // No target defaults to self as the target
                when (fieldSlot) {
                    FieldSlot.A -> listOf(ServantTarget.A)
                    FieldSlot.B -> listOf(ServantTarget.B)
                    FieldSlot.C -> listOf(ServantTarget.C)
                }
            }
            else -> tempTarget.targets
        }
    }

    private fun SkillSpamConfig.canSpamFinal(npCharged: Boolean) : Boolean {
        val npCond = when (np) {
            NpGaugeEnum.Low -> !npCharged
            NpGaugeEnum.Ready -> npCharged
            else -> true
        }

        return npCond
    }

    private fun FieldSlot.isNpCharged() : Boolean = locations.battle.npGaugeEndRegion(this)
            .isSaturationAndValueOver(
                SATURATION_THRESH,
                VALUE_THRESH
            )
}