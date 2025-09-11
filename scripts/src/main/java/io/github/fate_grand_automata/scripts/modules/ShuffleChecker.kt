package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.ShuffleCardsEnum
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ShuffleChecker @Inject constructor() {
    fun shouldShuffle(
        mode: ShuffleCardsEnum,
        cards: List<ParsedCard>,
        npUsage: NPUsage,
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
