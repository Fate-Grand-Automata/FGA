package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class AutoSkill @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val skillCommand: AutoSkillCommand,
    private val spamConfig: SpamConfigPerTeamSlot,
    private val caster: Caster
) : IFgoAutomataApi by fgAutomataApi {
    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> state.atk = action
        is AutoSkillAction.ServantSkill -> caster.castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> caster.castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> caster.selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> caster.orderChange(action)
    }

    fun canSpam(spam: SpamEnum): Boolean {
        val weCanSpam = spam == SpamEnum.Spam
        val weAreInDanger = spam == SpamEnum.Danger
                && state.chosenTarget != null

        return weCanSpam || weAreInDanger
    }

    val skillSpamDelay = Duration.seconds(0.25)

    private fun skillSpam() {
        for (servantSlot in FieldSlot.list) {
            val skills = servantSlot.skills()
            val teamSlot = servantTracker.deployed[servantSlot] ?: continue
            val servantSpamConfig = spamConfig[teamSlot]

            servantSpamConfig.skills.forEachIndexed { skillIndex, skillSpamConfig ->
                if (canSpam(skillSpamConfig.spam) && (state.stage + 1) in skillSpamConfig.waves) {
                    val skill = skills[skillIndex]
                    val skillImage = servantTracker
                        .checkImages[teamSlot]
                        ?.skills
                        ?.getOrNull(skillIndex)

                    if (skillImage != null) {
                        // Some delay for skill icon to be loaded
                        skillSpamDelay.wait()

                        if (skillImage in game.imageRegion(skill)) {
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

    fun execute() {
        val commandList = skillCommand[state.stage, state.turn]

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                act(action)
            }
        }

        skillSpam()
    }
}