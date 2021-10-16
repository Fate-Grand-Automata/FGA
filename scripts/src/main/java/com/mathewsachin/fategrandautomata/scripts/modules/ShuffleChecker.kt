package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.ParsedCard
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.models.toFieldSlot
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ShuffleChecker @Inject constructor(
    private val state: BattleState,
    private val battleConfig: IBattleConfig,
    private val servantTracker: ServantTracker
) {
    fun shouldShuffle(cards: List<ParsedCard>): Boolean {
        // Not this wave
        if (state.stage != (battleConfig.shuffleCardsWave - 1)) {
            return false
        }

        // Already shuffled
        if (state.shuffled) {
            return false
        }

        return when (battleConfig.shuffleCards) {
            ShuffleCardsEnum.None -> false
            ShuffleCardsEnum.NoEffective -> {
                val effectiveCardCount = cards
                    .count { it.affinity == CardAffinityEnum.Weak }

                effectiveCardCount == 0
            }
            ShuffleCardsEnum.NoNPMatching -> {
                if (state.atk.nps.isEmpty()) {
                    false
                } else {
                    val matchingCount = state.atk.nps
                        .mapNotNull { servantTracker.deployed[it.toFieldSlot()] }
                        .sumOf { teamSlot ->
                            cards.count { card -> card.servant == teamSlot }
                        }

                    matchingCount == 0
                }
            }
        }
    }
}