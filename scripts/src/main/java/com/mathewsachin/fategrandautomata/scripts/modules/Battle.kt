package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import kotlin.time.Duration

class Battle(fgAutomataApi: IFgoAutomataApi) : IFgoAutomataApi by fgAutomataApi {
    val state = BattleState()
    var servantTracker = ServantTracker(fgAutomataApi)
        private set
    val spamConfig = prefs.selectedBattleConfig.spam

    private lateinit var autoSkill: AutoSkill
    private lateinit var card: Card

    fun init(AutoSkillModule: AutoSkill, CardModule: Card) {
        autoSkill = AutoSkillModule
        card = CardModule

        state.markStartTime()

        resetState()
    }

    fun resetState() {
        // Don't increment no. of runs if we're just clicking on quest again and again
        // This can happen due to lags introduced during some events
        if (state.stage != -1) {
            state.nextRun()

            servantTracker.close()
            servantTracker = ServantTracker(this)
        }

        if (prefs.refill.shouldLimitRuns && state.runs >= prefs.refill.limitRuns) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.LimitRuns(state.runs))
        }
    }

    fun isIdle() = images[Images.BattleScreen] in game.battleScreenRegion

    fun clickAttack() {
        if (state.hasClickedAttack) {
            return
        }

        game.battleAttackClick.click()

        // Wait for Attack button to disappear
        game.battleScreenRegion.waitVanish(images[Images.BattleScreen], Duration.seconds(5))

        prefs.waitBeforeCards.wait()

        state.hasClickedAttack = true

        card.readCommandCards()
    }

    private fun isPriorityTarget(enemy: EnemyTarget): Boolean {
        val region = game.dangerRegion(enemy)

        val isDanger = images[Images.TargetDanger] in region
        val isServant = images[Images.TargetServant] in region

        return isDanger || isServant
    }

    private fun chooseTarget(enemy: EnemyTarget) {
        game.locate(enemy).click()

        Duration.seconds(0.5).wait()

        game.battleExtraInfoWindowCloseClick.click()
    }

    private fun autoChooseTarget() {
        // from my experience, most boss stages are ordered like(Servant 1)(Servant 2)(Servant 3),
        // where(Servant 3) is the most powerful one. see docs/ boss_stage.png
        // that's why the table is iterated backwards.

        val dangerTarget = EnemyTarget.list
            .lastOrNull { isPriorityTarget(it) }

        if (dangerTarget != null && state.chosenTarget != dangerTarget) {
            chooseTarget(dangerTarget)
        }

        state.chosenTarget = dangerTarget
    }

    fun performBattle() {
        prefs.waitBeforeTurn.wait()

        useSameSnapIn {
            onTurnStarted()

            servantTracker.beginTurn()
        }

        autoSkill.execute()

        clickAttack()

        card.clickCommandCards()

        Duration.seconds(5).wait()
    }

    private fun onTurnStarted() {
        checkCurrentStage()

        state.nextTurn()

        if (prefs.selectedBattleConfig.autoChooseTarget) {
            autoChooseTarget()
        }
    }

    private fun checkCurrentStage() {
        if (didStageChange()) {
            state.nextStage()

            takeStageSnapshot()
        }
    }

    fun didStageChange(): Boolean {
        // Alternative fix for different font of stage count number among different regions, worked pretty damn well tho.
        // This will compare last screenshot with current screen, effectively get to know if stage changed or not.
        val snapshot = state.stageCountSnaphot
            ?: return true

        return !game.battleStageCountRegion.exists(
            snapshot,
            similarity = prefs.stageCounterSimilarity
        )
    }

    fun takeStageSnapshot() {
        state.stageCountSnaphot =
            game.battleStageCountRegion.getPattern()
    }
}
