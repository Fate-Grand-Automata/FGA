package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Region
import kotlin.time.Duration
import kotlin.time.seconds

class AutoSkill(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    private lateinit var battle: Battle
    private lateinit var card: Card
    private var isFinished = false

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

    private val Skill.imageRegion
        get() = Region(30, 30, 30, 30) + clickLocation

    val skillSpamDelay = 0.25.seconds

    private fun castServantSkill(skill: Skill.Servant, target: ServantTarget?) {
        if (prefs.selectedBattleConfig.skillSpam != SpamEnum.None) {
            skillTable[skill]?.image?.close()

            // Some delay so we can take image of skill properly
            skillSpamDelay.wait()

            val image = skill.imageRegion.getPattern().tag("SKILL:$skill")
            skillTable[skill] = SkillTableEntry(target, image)
        }

        castSkill(skill, target)
    }

    private fun selectSkillTarget(target: ServantTarget) {
        target.clickLocation.click()

        0.5.seconds.wait()

        // Exit any extra menu
        Game.battleExtraInfoWindowCloseClick.click()
    }

    private fun openMasterSkillMenu() {
        Game.battleMasterSkillOpenClick.click()

        0.5.seconds.wait()
    }

    fun castMasterSkill(skill: Skill.Master, target: ServantTarget? = null) {
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

    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> card.atk = action
        is AutoSkillAction.ServantSkill -> castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> orderChange(action)
    }

    fun resetState() {
        isFinished = false

        skillTable.values.forEach { it.image.close() }
        skillTable.clear()
    }

    private data class SkillTableEntry(
        val target: ServantTarget?,
        val image: IPattern
    )

    private var skillTable = mutableMapOf<Skill.Servant, SkillTableEntry>()

    fun canSpam(spam: SpamEnum): Boolean {
        val weCanSpam = spam == SpamEnum.Spam
        val weAreInDanger = spam == SpamEnum.Danger
                && battle.state.hasChosenTarget

        return (weCanSpam || weAreInDanger) && isFinished
    }

    private fun skillSpam() {
        skillSpamDelay.wait()

        if (canSpam(prefs.selectedBattleConfig.skillSpam)) {
            for ((skill, entry) in skillTable) {
                if (entry.image in skill.imageRegion) {
                    castSkill(skill, entry.target)

                    // Some delay for skill icon to be loaded
                    skillSpamDelay.wait()
                }
            }
        }
    }

    lateinit var commandTable: AutoSkillCommand

    fun init(BattleModule: Battle, CardModule: Card) {
        battle = BattleModule
        card = CardModule

        commandTable = AutoSkillCommand.parse(
            prefs.selectedBattleConfig.skillCommand
        )

        resetState()
    }

    fun execute() {
        val stage = battle.state.stage
        val turn = battle.state.turn

        val commandList = commandTable[stage, turn]

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                act(action)
            }
        } else if (stage >= commandTable.lastStage) {
            // this will allow NP spam after all commands have been executed
            isFinished = true

            skillSpam()
        }
    }
}