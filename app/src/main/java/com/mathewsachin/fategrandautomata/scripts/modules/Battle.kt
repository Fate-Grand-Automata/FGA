package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.libautomata.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlin.time.seconds

class Battle {
    private var hasTakenFirstStageSnapshot = false

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

        hasTakenFirstStageSnapshot = false
        hasChosenTarget = false
        hasClickedAttack = false
    }

    fun isIdle() = Game.BattleScreenRegion.exists(ImageLocator.Battle)

    fun clickAttack() {
        Game.BattleAttackClick.click()

        // TODO: This was added extra in Kotlin impl
        // Wait for Attack button to disappear
        Game.BattleScreenRegion.waitVanish(ImageLocator.Battle, 5.seconds)

        // Although it seems slow, make it no shorter than 1 sec to protect user with less processing power devices.
        1.5.seconds.wait()

        hasClickedAttack = true

        card.readCommandCards()
    }

    private fun isPriorityTarget(Target: Region): Boolean {
        val isDanger = Target.exists(ImageLocator.TargetDanger)
        val isServant = Target.exists(ImageLocator.TargetServant)

        return isDanger || isServant
    }

    private fun chooseTarget(Index: Int) {
        Game.BattleTargetClickArray[Index].click()

        0.5.seconds.wait()

        Game.BattleExtrainfoWindowCloseClick.click()

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

        for ((i, target) in Game.BattleTargetRegionArray.withIndex().reversed()) {
            if (isPriorityTarget(target)) {
                chooseTarget(i)
                return
            }
        }
    }

    fun performBattle() {
        ScreenshotManager.useSameSnapIn { onTurnStarted() }
        2.seconds.wait()

        var wereNpsClicked = false

        if (Preferences.EnableAutoSkill) {
            wereNpsClicked = autoSkill.execute()

            autoSkill.resetNpTimer()
        }

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

        (if (wereNpsClicked) 25 else 5).seconds.wait()
    }

    private fun onTurnStarted() {
        checkCurrentStage()

        ++currentTurn

        hasClickedAttack = false

        if (!hasChosenTarget && Preferences.BattleAutoChooseTarget) {
            autoChooseTarget()
        }
    }

    private fun checkCurrentStage() {
        if (!hasTakenFirstStageSnapshot || didStageChange()) {
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

        return !Game.BattleStageCountRegion.exists(snapshot, Similarity = 0.85)
    }

    fun takeStageSnapshot() {
        generatedStageCounterSnapshot?.close()

        // It is important that the image gets cloned here.
        generatedStageCounterSnapshot = Game.BattleStageCountRegion.getPattern()

        hasTakenFirstStageSnapshot = true
    }
}
