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

@ScriptScope
class AttackPriorityHandler @Inject constructor(
    private val cardChainPriorityHandler: CardChainPriorityHandler,
    private val braveChainHandler: BraveChainHandler,
) {
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
        hasServantPriority: Boolean = false,
    ): List<ParsedCard> {
        var newCardOrder: List<ParsedCard>? = null
        var braveChainFallback: List<ParsedCard>? = null
        for (attackPriority in attackPriorityOrder) {
            when (attackPriority) {
                AttackPriorityEnum.BraveChainPriority -> {
                    braveChainFallback = braveChainHandler.pick(
                        cards = cards,
                        braveChainEnum = braveChainEnum,
                        npUsage = npUsage,
                    )
                }
                AttackPriorityEnum.CardChainPriority -> {
                    if (newCardOrder != null) continue
                    newCardOrder = cardChainPriorityHandler.pick(
                        cards = cards,
                        chainPriority = chainPriority,
                        braveChainEnum = braveChainEnum,
                        npUsage = npUsage,
                        npTypes = npTypes,
                        hasServantPriority = hasServantPriority,
                        // BraveChain is higher priority than color chain
                        forceBraveChain = braveChainFallback != null
                    )
                }
                else -> continue
            }
        }

        return rearrange(
            cards = newCardOrder ?: braveChainFallback ?: cards,
            rearrange = rearrange,
            npUsage = npUsage
        )
    }
}