package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.SkillSpamConfig
import io.github.fate_grand_automata.scripts.models.SkillSpamTarget
import io.github.fate_grand_automata.scripts.models.SpamConfigPerTeamSlot
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
    }

    fun spamSkills() {
        for (servantSlot in FieldSlot.list) {
            val skills = servantSlot.skills()
            val teamSlot = servantTracker.deployed[servantSlot] ?: continue
            val servantSpamConfig = spamConfig[teamSlot]

            servantSpamConfig.skills.forEachIndexed { skillIndex, skillSpamConfig ->
                if (caster.canSpam(skillSpamConfig.spam) && (state.stage + 1) in skillSpamConfig.waves) {
                    val skill = skills[skillIndex]
                    val skillImage = servantTracker
                        .checkImages[teamSlot]
                        ?.skills
                        ?.getOrNull(skillIndex)

                    if (skillImage != null) {
                        // Some delay for skill icon to be loaded
                        skillSpamDelay.wait()

                        if (skillImage in locations.battle.imageRegion(skill)) {
                            val target = skillSpamConfig.determineTarget(servantSlot)

                            caster.castServantSkill(skill, target)
                        }
                    }
                }
            }
        }
    }

    private fun SkillSpamConfig.determineTarget(fieldSlot: FieldSlot) =
        when (target) {
            SkillSpamTarget.None -> null
            SkillSpamTarget.Self -> when (fieldSlot) {
                FieldSlot.A -> ServantTarget.A
                FieldSlot.B -> ServantTarget.B
                FieldSlot.C -> ServantTarget.C
            }

            SkillSpamTarget.Slot1 -> ServantTarget.A
            SkillSpamTarget.Slot2 -> ServantTarget.B
            SkillSpamTarget.Slot3 -> ServantTarget.C
            SkillSpamTarget.Left -> ServantTarget.Left
            SkillSpamTarget.Right -> ServantTarget.Right
        }
}