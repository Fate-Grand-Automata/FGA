package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.fategrandautomata.scripts.models.ParsedCard
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class Battle @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val servantTracker: ServantTracker,
    private val state: BattleState,
    private val battleConfig: IBattleConfig,
    private val autoSkill: AutoSkill,
    private val caster: Caster,
    private val card: Card,
    private val skillSpam: SkillSpam,
    private val shuffleChecker: ShuffleChecker
) : IFgoAutomataApi by fgAutomataApi {
    init {
        state.markStartTime()

        resetState()
    }

    fun resetState() {
        // Don't increment no. of runs if we're just clicking on quest again and again
        // This can happen due to lags introduced during some events
        if (state.stage != -1) {
            state.nextRun()

            servantTracker.nextRun()
        }

        if (prefs.refill.shouldLimitRuns && state.runs >= prefs.refill.limitRuns) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.LimitRuns(state.runs))
        }
    }

    fun isIdle() = images[Images.BattleScreen] in game.battleScreenRegion

    fun clickAttack(): List<ParsedCard> {
        game.battleAttackClick.click()

        // Wait for Attack button to disappear
        game.battleScreenRegion.waitVanish(images[Images.BattleScreen], Duration.seconds(5))

        prefs.waitBeforeCards.wait()

        return card.readCommandCards()
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

        onTurnStarted()
        servantTracker.beginTurn()

        autoSkill.execute()
        skillSpam.spamSkills()

        var cards = clickAttack()

        if (shuffleChecker.shouldShuffle(cards)) {
            cards = shuffleCards()
        }
        card.clickCommandCards(cards)

        Duration.seconds(5).wait()
    }

    private fun shuffleCards(): List<ParsedCard> {
        game.battleBack.click()

        caster.castMasterSkill(Skill.Master.C)
        state.shuffled = true

        return clickAttack()
    }

    private fun onTurnStarted() = useSameSnapIn {
        checkCurrentStage()

        state.nextTurn()

        if (battleConfig.autoChooseTarget) {
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
        // Font of stage count number is different per region
        val snapshot = state.stageCountSnapshot
            ?: return true

        val matched = if (prefs.stageCounterNew) {
            // Take a screenshot of stage counter region on current screen and extract white pixels
            val current = game.battleStageCountRegion
                .getPattern()
                .tag("STAGE-COUNTER")

            current.use {
                val currentWithThreshold = current
                    .threshold(stageCounterThreshold)

                currentWithThreshold.use {
                    // Matching the images which have background filtered out
                    snapshot
                        .findMatches(currentWithThreshold, prefs.platformPrefs.minSimilarity)
                        .any()
                }
            }
        }
        else {
            // Compare last screenshot with current screen to determine if stage changed or not.
            game.battleStageCountRegion.exists(
                snapshot,
                similarity = prefs.stageCounterSimilarity
            )
        }

        return !matched
    }

    private val stageCounterThreshold = 0.67

    fun takeStageSnapshot() {
        state.stageCountSnapshot =
            game.battleStageCountRegion.getPattern().tag("WAVE:${state.stage}")

        if (prefs.stageCounterNew) {
            // Extract white pixels from the image which gets rid of the background.
            state.stageCountSnapshot =
                state.stageCountSnapshot?.threshold(stageCounterThreshold)
        }
    }
}
