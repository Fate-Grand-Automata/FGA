package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.EnemyTarget
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.libautomata.IPattern
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

    private lateinit var autoSkill: AutoSkill
    private lateinit var card: Card

    fun init(AutoSkillModule: AutoSkill, CardModule: Card) {
        autoSkill = AutoSkillModule
        card = CardModule

        resetState()
    }

    fun resetState() {
        autoSkill.resetState()

        currentStage = -1
        currentTurn = -1

        generatedStageCounterSnapshot = null
        hasChosenTarget = false
        hasClickedAttack = false
    }

    fun isIdle() = game.battleScreenRegion.exists(images.battle)

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
        val isDanger = enemyTarget.region.exists(images.targetDanger)
        val isServant = enemyTarget.region.exists(images.targetServant)

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

        val wereNpsClicked = autoSkill.execute()

        autoSkill.resetNpTimer()

        if (!hasClickedAttack) {
            clickAttack()
        }

        if (card.canClickNpCards) {
            // We shouldn't do the long wait due to NP spam/danger modes
            // They click on NPs even when not charged
            // So, don't assign wereNpsClicked here
            card.clickNpCards()
        }

        card.clickCommandCards(5)

        card.resetCommandCards()

        (if (wereNpsClicked) 15 else 5).seconds.wait()
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

        return !game.battleStageCountRegion.exists(snapshot, Similarity = 0.85)
    }

    fun takeStageSnapshot() {
        generatedStageCounterSnapshot?.close()

        generatedStageCounterSnapshot = game.battleStageCountRegion.getPattern()
    }
}
