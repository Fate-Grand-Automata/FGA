package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.NPUsage
import com.mathewsachin.fategrandautomata.scripts.models.ParsedCard
import com.mathewsachin.fategrandautomata.scripts.models.toFieldSlot
import com.mathewsachin.libautomata.dagger.ScriptScope
import java.util.*
import javax.inject.Inject

@ScriptScope
class ApplyBraveChains @Inject constructor() {
    private fun rearrange(
        cards: List<ParsedCard>,
        rearrange: Boolean,
        npUsage: NPUsage
    ): List<ParsedCard> {
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

    private fun withNp(
        cards: List<ParsedCard>,
        rearrange: Boolean,
        npUsage: NPUsage
    ): List<ParsedCard> {
        val justRearranged by lazy {
            rearrange(
                cards = cards.take(3),
                rearrange = rearrange,
                npUsage = npUsage
            )
        }

        val firstNp = npUsage.nps.firstOrNull() ?: return justRearranged
        val fieldSlot = firstNp.toFieldSlot()

        val matchingCards = cards
            .filter { it.fieldSlot == fieldSlot }
            .toMutableList()

        // When there is 1 NP, 1 Card before NP, only 1 matching face-card,
        // we want the matching face-card after NP.
        if (rearrange
            && listOf(npUsage.nps.size, npUsage.cardsBeforeNP, matchingCards.size).all { it == 1 }
        ) {
            Collections.swap(matchingCards, 0, 1)
        }

        val nonMatchingCards = cards - matchingCards

        return rearrange(
            cards = matchingCards + nonMatchingCards,
            rearrange = rearrange,
            npUsage = npUsage
        )
    }

    private fun avoid(
        cards: List<ParsedCard>,
        rearrange: Boolean
    ): List<ParsedCard> {
        val cardsGroupedByServant = cards.groupBy { it.servant }.values

        if (cardsGroupedByServant.size > 1) {
            if (rearrange) {
                // Top 3 priority cards grouped by servant
                val topGrouped = cards
                    .take(3)
                    .groupBy { it.servant }
                    .map { it.value }

                when (topGrouped.size) {
                    // All 3 cards of same servant
                    1 -> {
                        val group = cardsGroupedByServant.first { topGrouped[0][0] in it }

                        // Check if there's another servant
                        val otherCard = cards
                            .firstOrNull { it !in group }

                        // If there's no other servant, this will fallback to default card picker
                        if (otherCard != null) {
                            return listOf(
                                topGrouped[0][0],
                                otherCard,
                                topGrouped[0][1]
                            )
                        }
                    }
                    // Ideal case. 2 servant cards
                    2 -> {
                        // servant with 2 cards in first place
                        val topSorted = topGrouped
                            .sortedByDescending { it.size }

                        return listOf(
                            topSorted[0][0],
                            topSorted[1][0],
                            topSorted[0][1]
                        )
                    }
                    // Brave chain will already be avoided, but we can rearrange to optimize
                    3 -> {
                        return listOf(
                            topGrouped[0][0],
                            topGrouped[2][0],
                            topGrouped[1][0]
                        )
                    }
                }
            } else return legacyAvoidBraveChains(cards, cardsGroupedByServant)
        }

        return emptyList()
    }

    private fun legacyAvoidBraveChains(
        cards: List<ParsedCard>,
        cardsGroupedByServant: Collection<List<ParsedCard>>
    ): List<ParsedCard> {
        val pickedCards = mutableListOf<ParsedCard>()
        val remainingCards = cards.toMutableList()
        var lastGroup = emptyList<ParsedCard>()

        while (true) {
            val otherServantCard = remainingCards.firstOrNull { it !in lastGroup } ?: break
            lastGroup = cardsGroupedByServant.firstOrNull { otherServantCard in it } ?: break

            pickedCards += otherServantCard
            remainingCards -= otherServantCard
        }

        return pickedCards
    }

    fun pick(
        cards: List<ParsedCard>,
        braveChains: BraveChainEnum,
        rearrange: Boolean = false,
        npUsage: NPUsage = NPUsage.none
    ): List<ParsedCard> {
        val picked = when (braveChains) {
            BraveChainEnum.None -> rearrange(
                cards = cards.take(3),
                rearrange = rearrange,
                npUsage = npUsage
            )
            BraveChainEnum.WithNP -> withNp(
                cards = cards,
                rearrange = rearrange,
                npUsage = npUsage
            )
            BraveChainEnum.Avoid -> avoid(
                cards = cards,
                rearrange = rearrange
            )
        }

        val notPicked = cards - picked

        return picked + notPicked
    }
}