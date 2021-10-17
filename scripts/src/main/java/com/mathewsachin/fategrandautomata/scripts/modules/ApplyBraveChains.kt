package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import java.util.*
import javax.inject.Inject

@ScriptScope
class ApplyBraveChains @Inject constructor(
    private val state: BattleState,
    private val servantTracker: ServantTracker,
    battleConfig: IBattleConfig
) {
    private fun <T> List<T>.inCurrentWave(default: T) =
        if (isNotEmpty())
            this[state.stage.coerceIn(indices)]
        else default

    private val braveChainsPerWave = battleConfig.braveChains
    private val rearrangeCardsPerWave = battleConfig.rearrangeCards

    private fun rearrange(
        cards: List<ParsedCard>,
        rearrange: Boolean,
        atk: AutoSkillAction.Atk
    ): List<ParsedCard> {
        if (rearrange
            // If there are cards before NP, at max there's only 1 card after NP
            && atk.cardsBeforeNP == 0
            // If there are more than 1 NPs, only 1 card after NPs at max
            && atk.nps.size <= 1
        ) {
            val cardsToRearrange = cards
                .mapIndexed { index, _ -> index }
                .take((3 - atk.nps.size).coerceAtLeast(0))
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
        atk: AutoSkillAction.Atk,
        deployed: Map<FieldSlot, TeamSlot>
    ): List<ParsedCard> {
        val firstNp = atk.nps.firstOrNull() ?: return emptyList()
        val fieldSlot = firstNp.toFieldSlot()
        val teamSlot = deployed[fieldSlot] ?: return emptyList()

        val matchingCards = cards
            .filter { it.servant == teamSlot }
            .toMutableList()

        // When there is 1 NP, 1 Card before NP, only 1 matching face-card,
        // we want the matching face-card after NP.
        if (rearrange
            && listOf(atk.nps.size, atk.cardsBeforeNP, matchingCards.size).all { it == 1 }
        ) {
            Collections.swap(matchingCards, 0, 1)
        }

        return rearrange(
            cards = matchingCards,
            rearrange = rearrange,
            atk = atk
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
            } else legacyAvoidBraveChains(cards, cardsGroupedByServant)
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

            pickedCards.add(otherServantCard)
            remainingCards.remove(otherServantCard)
        }

        return pickedCards
    }

    fun pick(
        cards: List<ParsedCard>,
        braveChains: BraveChainEnum = braveChainsPerWave.inCurrentWave(BraveChainEnum.None),
        rearrange: Boolean = rearrangeCardsPerWave.inCurrentWave(false),
        atk: AutoSkillAction.Atk = state.atk,
        deployed: Map<FieldSlot, TeamSlot> = servantTracker.deployed
    ): List<ParsedCard> {
        val picked = when (braveChains) {
            BraveChainEnum.None -> rearrange(
                cards = cards.take(3),
                rearrange = rearrange,
                atk = atk
            )
            BraveChainEnum.WithNP -> withNp(
                cards = cards,
                rearrange = rearrange,
                atk = atk,
                deployed = deployed
            )
            BraveChainEnum.Avoid -> avoid(
                cards = cards,
                rearrange = rearrange
            )
        }

        val notPicked = cards.filter { it !in picked }

        return picked + notPicked
    }
}