package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.ShuffleCardsEnum
import com.mathewsachin.fategrandautomata.scripts.models.NPUsage
import com.mathewsachin.fategrandautomata.scripts.models.ParsedCard
import com.mathewsachin.fategrandautomata.scripts.models.toFieldSlot
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ShuffleChecker @Inject constructor() {
    fun shouldShuffle(
        mode: ShuffleCardsEnum,
        cards: List<ParsedCard>,
        npUsage: NPUsage
    ): Boolean = when (mode) {
        ShuffleCardsEnum.None -> false
        ShuffleCardsEnum.NoEffective -> {
            val effectiveCardCount = cards
                .count { it.affinity == CardAffinityEnum.Weak }

            effectiveCardCount == 0
        }
        ShuffleCardsEnum.NoNPMatching -> {
            if (npUsage.nps.isEmpty()) {
                false
            } else {
                val matchingCount = npUsage.nps
                    .map { it.toFieldSlot() }
                    .sumOf { fieldSlot ->
                        cards.count { card -> card.fieldSlot == fieldSlot }
                    }

                matchingCount == 0
            }
        }
    }
}