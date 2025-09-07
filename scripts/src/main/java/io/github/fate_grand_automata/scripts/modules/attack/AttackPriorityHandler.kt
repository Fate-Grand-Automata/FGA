package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.AttackPriorityEnum
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import java.util.Collections
import javax.inject.Inject

object AttackPriorityHandler {
    fun rearrange(
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
        attackPriorityOrder: List<AttackPriorityEnum> = AttackPriorityEnum.defaultOrder,
        chainPriority: List<ChainTypeEnum> = ChainTypeEnum.defaultOrder,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        rearrange: Boolean = false,
    ): List<ParsedCard> {
        // All stunned cards are categorized as Unknown.
        // Filter all of them since those cards are to be avoided if possible
        // and the system does not deal with CardTypeEnum.Unknown
        val nonUnknownCards = cards.filter { it.type != CardTypeEnum.Unknown }
        val finalFallback = nonUnknownCards + (cards - nonUnknownCards)
        if (!AttackUtils.isChainable(
                cards = nonUnknownCards,
                npUsage = npUsage,
                npTypes = npTypes,
            )) {
            return finalFallback
        }

        // Get all the supplementary data
        val cardCountPerFieldSlotMap = AttackUtils.getCardsPerFieldSlotMap(cards, npUsage)
        val cardCountPerCardTypeMap = AttackUtils.getCardsPerCardTypeMap(cards, npTypes)

        // Start
        var newCardOrder: List<ParsedCard>? = null
        var braveChainFallback: List<ParsedCard>? = null
        for (attackPriority in attackPriorityOrder) {
            when (attackPriority) {
                AttackPriorityEnum.BraveChainPriority -> {
                    braveChainFallback = BraveChainHandler.pick(
                        cards = nonUnknownCards,
                        braveChainEnum = braveChainEnum,
                        npUsage = npUsage,
                        cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                    )
                }
                AttackPriorityEnum.CardChainPriority -> {
                    if (newCardOrder != null) continue

                    // Determine the allowed chain priority
                    val indexOfAvoid = chainPriority.indexOf(ChainTypeEnum.Avoid)
                    // If braveChainFallback does not exist,
                    // 'Avoid' is to be included, since it is treated as cardPriorityHandler's own fallback method
                    val allowAvoid = if (braveChainFallback == null) 1 else 0
                    val filteredChainPriority = when (indexOfAvoid) {
                        -1 -> chainPriority
                        0 -> listOf(ChainTypeEnum.Avoid)
                        else -> chainPriority.subList(0, indexOfAvoid + allowAvoid)
                    }

                    newCardOrder = CardChainPriorityHandler.pick(
                        cards = nonUnknownCards,
                        chainPriority = filteredChainPriority,
                        braveChainEnum = braveChainEnum,
                        npUsage = npUsage,
                        npTypes = npTypes,
                        cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                        cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                        // BraveChain is higher priority than color chain
                        forceBraveChain = braveChainFallback != null
                                && braveChainEnum != BraveChainEnum.Avoid
                                && braveChainEnum != BraveChainEnum.None
                    )
                }
                else -> continue
            }
        }

        return rearrange(
            cards = newCardOrder ?: braveChainFallback ?: finalFallback,
            rearrange = rearrange,
            npUsage = npUsage
        )
    }
}