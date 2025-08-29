package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.modules.ApplyBraveChains
import io.github.lib_automata.dagger.ScriptScope
import java.util.Collections
import javax.inject.Inject

@ScriptScope
class ChainPrioritySelector @Inject constructor(
    private val applyMightyChains: ApplyMightyChains,
) {
    private fun rearrange(
        cards: List<ParsedCard>,
        rearrange: Boolean,
        npUsage: NPUsage
    ): List<ParsedCard> {
        /*
          When rearrange is active and there is 1 NP and 1 Card before NP,
          we want the best or matching face-card after NP.
         */
        val shouldSwapForNpUsageScenario = listOf(npUsage.nps.size, npUsage.cardsBeforeNP).all { it == 1 }
        if (shouldSwapForNpUsageScenario) {
            return cards.toMutableList().also {
                Collections.swap(it, 0, 1)
            }
        }

        if (rearrange
            // If there are cards before NP, at max there's only 1 card after NP
            && npUsage.cardsBeforeNP == 0
            // If there are more than 1 NPs, only 1 card after NPs at max
            && npUsage.nps.size <= 1
        ) {
            val cardsToRearrange = List(cards.size) { index -> index }
                .take((3 - npUsage.nps.size).coerceAtLeast(0))
                .reversed()

            // When clicking 3 cards, move the card with 2nd highest priority to last position to amplify its effect
            // Do the same when clicking 2 cards unless they're used before NPs.
            if (cardsToRearrange.size in 2..3) {
                return cards.toMutableList().also {
                    Collections.swap(it, cardsToRearrange[1], cardsToRearrange[0])
                }
            }
        }

        return cards
    }

    fun pick(
        cards: List<ParsedCard>,
        chainPriority: List<ChainTypeEnum>,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap()
    ): List<ParsedCard> {
        var newCardOrder: List<ParsedCard>? = null
        for (chain in chainPriority) {
            newCardOrder = when (chain) {
                ChainTypeEnum.Mighty -> applyMightyChains.getMightyChain(cards, npUsage, npTypes)
                else -> null
            }
            if (newCardOrder != null) break;
        }

        return rearrange(
            cards = newCardOrder ?: cards,
            rearrange = rearrange,
            npUsage = npUsage
        )
    }
}