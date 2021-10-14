package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
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
    private val spamConfig: SpamConfigPerTeamSlot
) : IFgoAutomataApi by fgAutomataApi {
    private fun waitForAnimationToFinish(timeout: Duration = Duration.seconds(5)) {
        val img = images[Images.BattleScreen]

        // slow devices need this. do not remove.
        game.battleScreenRegion.waitVanish(img, Duration.seconds(2))

        game.battleScreenRegion.exists(img, timeout)
    }

    private fun confirmSkillUse() {
        if (prefs.skillConfirmation) {
            game.battleSkillOkClick.click()
        }
    }

    private fun castSkill(skill: Skill, target: ServantTarget?) {
        game.locate(skill).click()
        confirmSkillUse()

        if (target != null) {
            prefs.skillDelay.wait()

            selectSkillTarget(target)
        } else {
            // Close the window that opens up if skill is on cool-down
            game.battleExtraInfoWindowCloseClick.click()
        }

        waitForAnimationToFinish()
    }

    private fun castServantSkill(skill: Skill.Servant, target: ServantTarget?) {
        castSkill(skill, target)
    }

    private fun selectSkillTarget(target: ServantTarget) {
        val actualTarget = when (target) {
            ServantTarget.Left, ServantTarget.Right -> target
            else -> {
                val deployed = servantTracker.deployed

                // How many servants on field?
                when (deployed.size) {
                    1 -> ServantTarget.B
                    2 -> {
                        when (target) {
                            ServantTarget.A -> ServantTarget.Left
                            ServantTarget.C -> ServantTarget.Right
                            ServantTarget.B -> {
                                when (null) {
                                    deployed[FieldSlot.A] -> ServantTarget.Left
                                    deployed[FieldSlot.C] -> ServantTarget.Right
                                    else -> ServantTarget.Left // Assume Left when Slot B is empty
                                }
                            }
                            else -> target
                        }
                    }
                    else -> target
                }
            }
        }

        game.locate(actualTarget).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun openMasterSkillMenu() {
        game.battleMasterSkillOpenClick.click()

        Duration.seconds(0.5).wait()
    }

    fun castMasterSkill(skill: Skill.Master, target: ServantTarget? = null) {
        openMasterSkillMenu()

        castSkill(skill, target)
    }

    private fun orderChange(action: AutoSkillAction.OrderChange) {
        openMasterSkillMenu()

        // Click on order change skill
        game.locate(Skill.Master.C).click()

        confirmSkillUse()

        Duration.seconds(0.3).wait()

        game.locate(action.starting).click()
        game.locate(action.sub).click()

        Duration.seconds(0.3).wait()

        game.battleOrderChangeOkClick.click()

        // Extra wait to allow order change dialog to close
        Duration.seconds(1).wait()

        waitForAnimationToFinish(Duration.seconds(15))

        // Extra wait for the lag introduced by Order change
        Duration.seconds(1).wait()

        servantTracker.orderChanged(action.starting, action.sub)
    }

    private fun selectEnemyTarget(enemy: EnemyTarget) {
        game.locate(enemy).click()

        Duration.seconds(0.5).wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> state.atk = action
        is AutoSkillAction.ServantSkill -> castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> orderChange(action)
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

                            castSkill(skill, target)
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