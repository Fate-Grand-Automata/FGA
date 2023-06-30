package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class StageTracker @Inject constructor(
    api: IFgoAutomataApi,
    private val state: BattleState
) : IFgoAutomataApi by api {
    fun checkCurrentStage() {
        if (didStageChange()) {
            state.nextStage()

            takeStageSnapshot()
        }
    }

    private fun didStageChange(): Boolean {
        // Font of stage count number is different per region
        val snapshot = state.stageCountSnapshot
            ?: return true

        val matched = if (prefs.stageCounterNew) {
            // Take a screenshot of stage counter region on current screen and extract white pixels
            val current = locations.battle.master.stageCountRegion
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
            locations.battle.master.stageCountRegion.exists(
                snapshot,
                similarity = prefs.stageCounterSimilarity
            )
        }

        return !matched
    }

    private val stageCounterThreshold = 0.67

    private fun takeStageSnapshot() {
        state.stageCountSnapshot =
            locations.battle.master.stageCountRegion.getPattern().tag("WAVE:${state.stage}")

        if (prefs.stageCounterNew) {
            // Extract white pixels from the image which gets rid of the background.
            state.stageCountSnapshot =
                state.stageCountSnapshot?.threshold(stageCounterThreshold)
        }
    }
}