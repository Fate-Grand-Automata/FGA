package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.libautomata.ScriptExitException
import kotlin.time.seconds

class Battle(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    val state = BattleState()

    private lateinit var autoSkill: AutoSkill
    private lateinit var card: Card

    fun init(AutoSkillModule: AutoSkill, CardModule: Card) {
        autoSkill = AutoSkillModule
        card = CardModule

        state.markStartTime()

        resetState()
    }

    fun resetState() {
        autoSkill.resetState()

        // Don't increment no. of runs if we're just clicking on quest again and again
        // This can happen due to lags introduced during some events
        if (state.runState.stage != -1) {
            state.nextRun()

            if (prefs.refill.shouldLimitRuns && state.runs >= prefs.refill.limitRuns) {
                throw ScriptExitException(messages.timesRan(state.runs))
            }
        }
    }

    fun isIdle() = images.battle in Game.battleScreenRegion

    fun clickAttack() {
        if (state.runState.turnState.hasClickedAttack) {
            return
        }

        Game.battleAttackClick.click()

        // Wait for Attack button to disappear
        Game.battleScreenRegion.waitVanish(images.battle, 5.seconds)

        // Although it seems slow, make it no shorter than 1 sec to protect user with less processing power devices.
        2.seconds.wait()

        state.runState.turnState.hasClickedAttack = true

        card.readCommandCards()
    }

    private fun isPriorityTarget(enemyTarget: EnemyTarget): Boolean {
        val isDanger = images.targetDanger in enemyTarget.region
        val isServant = images.targetServant in enemyTarget.region

        return isDanger || isServant
    }

    private fun chooseTarget(enemyTarget: EnemyTarget) {
        enemyTarget.clickLocation.click()

        0.5.seconds.wait()

        Game.battleExtraInfoWindowCloseClick.click()

        state.runState.stageState.hasChosenTarget = true
    }

    private fun autoChooseTarget() {
        // from my experience, most boss stages are ordered like(Servant 1)(Servant 2)(Servant 3),
        // where(Servant 3) is the most powerful one. see docs/ boss_stage.png
        // that's why the table is iterated backwards.

        EnemyTarget.list
            .lastOrNull { isPriorityTarget(it) }
            ?.let { chooseTarget(it) }
    }

    fun performBattle() {
        screenshotManager.useSameSnapIn { onTurnStarted() }
        prefs.waitBeforeTurn.wait()

        autoSkill.execute()

        clickAttack()

        if (card.canClickNpCards) {
            card.clickNpCards()
        }

        card.clickCommandCards()

        5.seconds.wait()
    }

    private fun onTurnStarted() {
        checkCurrentStage()

        state.runState.nextTurn()

        if (!state.runState.stageState.hasChosenTarget && prefs.autoChooseTarget) {
            autoChooseTarget()
        }
    }

    private fun checkCurrentStage() {
        if (didStageChange()) {
            state.runState.nextStage()

            takeStageSnapshot()
        }
    }

    fun didStageChange(): Boolean {
        // Alternative fix for different font of stage count number among different regions, worked pretty damn well tho.
        // This will compare last screenshot with current screen, effectively get to know if stage changed or not.
        val snapshot = state.runState.stageState.stageCountSnaphot
            ?: return true

        return !game.battleStageCountRegion.exists(
            snapshot,
            Similarity = prefs.stageCounterSimilarity
        )
    }

    fun takeStageSnapshot() {
        state.runState.stageState.stageCountSnaphot =
            game.battleStageCountRegion.getPattern()
    }
}
