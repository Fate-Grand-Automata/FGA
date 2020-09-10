package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.*
import kotlin.time.Duration
import kotlin.time.seconds

class AutoSkill(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private lateinit var battle: Battle
    private lateinit var card: Card

    var isFinished = false
        private set

    private fun waitForAnimationToFinish(Timeout: Duration = 5.seconds) {
        val img = images.battle

        // slow devices need this. do not remove.
        Game.battleScreenRegion.waitVanish(img, 2.seconds)

        Game.battleScreenRegion.exists(img, Timeout)
    }

    private fun castSkill(skill: Skill, target: ServantTarget?) {
        skill.clickLocation.click()

        if (prefs.skillConfirmation) {
            Game.battleSkillOkClick.click()
        }

        if (target != null) {
            prefs.skillDelay.wait()

            selectSkillTarget(target)
        } else {
            // Close the window that opens up if skill is on cool-down
            Game.battleExtraInfoWindowCloseClick.click()
        }

        waitForAnimationToFinish()
    }

    private fun selectSkillTarget(target: ServantTarget) {
        target.clickLocation.click()

        0.5.seconds.wait()

        // Exit any extra menu
        Game.battleExtraInfoWindowCloseClick.click()
    }

    private fun castNoblePhantasm(noblePhantasm: CommandCard.NP) {
        battle.clickAttack()

        card.clickNp(noblePhantasm)
    }

    private fun openMasterSkillMenu() {
        Game.battleMasterSkillOpenClick.click()

        0.5.seconds.wait()
    }

    private fun castMasterSkill(skill: Skill.Master, target: ServantTarget?) {
        openMasterSkillMenu()

        castSkill(skill, target)
    }

    private fun orderChange(action: AutoSkillAction.OrderChange) {
        openMasterSkillMenu()

        // Click on order change skill
        Skill.Master.list.last()
            .clickLocation.click()

        if (prefs.skillConfirmation) {
            Game.battleSkillOkClick.click()
        }

        0.3.seconds.wait()

        action.starting.clickLocation.click()
        action.sub.clickLocation.click()

        0.3.seconds.wait()

        Game.battleOrderChangeOkClick.click()

        // Extra wait to allow order change dialog to close
        1.seconds.wait()

        waitForAnimationToFinish(15.seconds)

        // Extra wait for the lag introduced by Order change
        1.seconds.wait()
    }

    private fun selectEnemyTarget(enemyTarget: EnemyTarget) {
        enemyTarget.clickLocation.click()

        0.5.seconds.wait()

        // Exit any extra menu
        Game.battleExtraInfoWindowCloseClick.click()
    }

    fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.CardsBeforeNP -> {
            battle.clickAttack()

            card.clickCommandCards(action.count)
        }
        is AutoSkillAction.NP -> castNoblePhantasm(action.np)
        is AutoSkillAction.ServantSkill -> castSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> orderChange(action)
        AutoSkillAction.NoOp -> Unit
    }

    fun resetState() {
        isFinished = false
    }

    lateinit var commandTable: AutoSkillCommand

    fun init(BattleModule: Battle, CardModule: Card) {
        battle = BattleModule
        card = CardModule

        commandTable = AutoSkillCommand.parse(
            prefs.selectedAutoSkillConfig.skillCommand
        )

        resetState()
    }

    fun execute() {
        val stage = battle.state.runState.stage
        val turn = battle.state.runState.turn

        val commandList = commandTable[stage, turn]

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                act(action)
            }
        } else if (stage >= commandTable.lastStage) {
            // this will allow NP spam after all commands have been executed
            isFinished = true
        }
    }
}