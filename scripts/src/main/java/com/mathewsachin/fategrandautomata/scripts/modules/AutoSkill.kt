package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import kotlin.time.Duration
import kotlin.time.seconds

class AutoSkill(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private lateinit var battle: Battle
    private lateinit var card: Card

    private fun waitForAnimationToFinish(Timeout: Duration = 5.seconds) {
        val img = images.battle

        // slow devices need this. do not remove.
        game.battleScreenRegion.waitVanish(img, 2.seconds)

        game.battleScreenRegion.exists(img, Timeout)
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
        game.locate(target).click()

        0.5.seconds.wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun openMasterSkillMenu() {
        game.battleMasterSkillOpenClick.click()

        0.5.seconds.wait()
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

        0.3.seconds.wait()

        game.locate(action.starting).click()
        game.locate(action.sub).click()

        0.3.seconds.wait()

        game.battleOrderChangeOkClick.click()

        // Extra wait to allow order change dialog to close
        1.seconds.wait()

        waitForAnimationToFinish(15.seconds)

        // Extra wait for the lag introduced by Order change
        1.seconds.wait()

        battle.servantTracker.orderChanged(action.starting, action.sub)
    }

    private fun selectEnemyTarget(enemy: EnemyTarget) {
        game.locate(enemy).click()

        0.5.seconds.wait()

        // Exit any extra menu
        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> card.atk = action
        is AutoSkillAction.ServantSkill -> castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> orderChange(action)
    }

    fun canSpam(spam: SpamEnum): Boolean {
        val weCanSpam = spam == SpamEnum.Spam
        val weAreInDanger = spam == SpamEnum.Danger
                && battle.state.hasChosenTarget

        return weCanSpam || weAreInDanger
    }

    val skillSpamDelay = 0.25.seconds

    private fun skillSpam() {
        ServantSlot.list.forEach { servantSlot ->
            val skills = servantSlot.skills()
            val teamSlot = battle.servantTracker.deployed[servantSlot] ?: ServantTracker.TeamSlot.A
            val servantSpamConfig = battle.spamConfig.getOrElse(teamSlot.position - 1) { ServantSpamConfig() }

            servantSpamConfig.skills.forEachIndexed { skillIndex, skillSpamConfig ->
                if (canSpam(skillSpamConfig.spam) && (battle.state.stage + 1) in skillSpamConfig.waves) {
                    val skill = skills[skillIndex]
                    val skillImage = battle.servantTracker
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

    private fun SkillSpamConfig.determineTarget(servantSlot: ServantSlot) =
        when (target) {
            SkillSpamTarget.None -> null
            SkillSpamTarget.Self -> when (servantSlot) {
                ServantSlot.A -> ServantTarget.A
                ServantSlot.B -> ServantTarget.B
                ServantSlot.C -> ServantTarget.C
            }
            SkillSpamTarget.Slot1 -> ServantTarget.A
            SkillSpamTarget.Slot2 -> ServantTarget.B
            SkillSpamTarget.Slot3 -> ServantTarget.C
            SkillSpamTarget.Left -> ServantTarget.Left
            SkillSpamTarget.Right -> ServantTarget.Right
        }

    lateinit var commandTable: AutoSkillCommand

    fun init(BattleModule: Battle, CardModule: Card) {
        battle = BattleModule
        card = CardModule

        commandTable = AutoSkillCommand.parse(
            prefs.selectedBattleConfig.skillCommand
        )
    }

    fun execute() {
        val stage = battle.state.stage
        val turn = battle.state.turn

        val commandList = commandTable[stage, turn]

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                act(action)
            }
        }

        skillSpam()
    }
}