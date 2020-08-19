package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.ScriptExitException
import kotlin.time.seconds

class Battle(fgAutomataApi: IFGAutomataApi) : IFGAutomataApi by fgAutomataApi {
    var hasClickedAttack = false
        private set

    var hasChosenTarget = false
        private set

    var currentStage = -1
        private set

    var currentTurn = -1
        private set

    var runs = 0
        private set

    private lateinit var autoSkill: AutoSkill
    private lateinit var card: Card

    fun init(AutoSkillModule: AutoSkill, CardModule: Card) {
        autoSkill = AutoSkillModule
        card = CardModule

        resetState()
    }

    fun resetState() {
        autoSkill.resetState()

        // Don't increment no. of runs if we're just clicking on quest again and again
        // This can happen due to lags introduced during some events
        if (currentStage != -1) {
            ++runs

            if (prefs.refill.shouldLimitRuns && runs >= prefs.refill.limitRuns) {
                throw ScriptExitException("Ran $runs time(s)")
            }
        }

        currentStage = -1
        currentTurn = -1

        generatedStageCounterSnapshot = null
        hasChosenTarget = false
        hasClickedAttack = false
    }

    fun isIdle() = images.battle in game.battleScreenRegion

    fun clickAttack() {
        game.battleAttackClick.click()

        // TODO: This was added extra in Kotlin impl
        // Wait for Attack button to disappear
        game.battleScreenRegion.waitVanish(images.battle, 5.seconds)

        // Although it seems slow, make it no shorter than 1 sec to protect user with less processing power devices.
        1.5.seconds.wait()

        hasClickedAttack = true

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

        game.battleExtraInfoWindowCloseClick.click()

        hasChosenTarget = true
    }

    private fun onStageChanged() {
        ++currentStage
        currentTurn = -1
        hasChosenTarget = false
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
        2.seconds.wait()

        autoSkill.execute()

        if (!hasClickedAttack) {
            clickAttack()
        }

        if (card.canClickNpCards) {
            card.clickNpCards()
        }

        card.clickCommandCards()

        5.seconds.wait()
    }

    private fun onTurnStarted() {
        checkCurrentStage()

        ++currentTurn

        hasClickedAttack = false

        if (!hasChosenTarget && prefs.autoChooseTarget) {
            autoChooseTarget()
        }
    }

    private fun checkCurrentStage() {
        if (didStageChange()) {
            onStageChanged()

            takeStageSnapshot()
        }
    }

    private var generatedStageCounterSnapshot: IPattern? = null

    fun didStageChange(): Boolean {
        // Alternative fix for different font of stage count number among different regions, worked pretty damn well tho.
        // This will compare last screenshot with current screen, effectively get to know if stage changed or not.
        val snapshot = generatedStageCounterSnapshot
            ?: return true

        return !game.battleStageCountRegion.exists(
            snapshot,
            Similarity = prefs.stageCounterSimilarity
        )
    }

    fun takeStageSnapshot() {
        generatedStageCounterSnapshot?.close()

        generatedStageCounterSnapshot = game.battleStageCountRegion.getPattern()
    }
}
