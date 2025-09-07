package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot

object BraveChainHandler {
    fun pick(
        npUsage: NPUsage = NPUsage.none,
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        if (braveChainEnum == BraveChainEnum.Avoid) {
            return AvoidChainHandler.pick(
                cards = cards,
                npUsage = npUsage,
                avoidBraveChains = true,
                avoidCardChains = false,
            )
        }

        // Try to ensure unknown is handled
        val nonUnknownCards = AttackUtils.getValidNonUnknownCards(cards)
        if (!AttackUtils.isChainable(
            cards = nonUnknownCards,
            npUsage = npUsage,
        )) {
            return null
        }

        val cardsPerFieldSlotMap = cardCountPerFieldSlotMap ?: AttackUtils.getCardsPerFieldSlotMap(nonUnknownCards, npUsage)
        if (!isBraveChainAllowed(braveChainEnum, npUsage, cardsPerFieldSlotMap)) return null

        // Always returns a valid field slot if there is one for BraveChain
        val priorityFieldSlot = AttackUtils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = nonUnknownCards,
            npUsage = npUsage,
        )
        if (priorityFieldSlot == null) return null

        val selectedCards = nonUnknownCards
            .filter { it.fieldSlot == priorityFieldSlot }
            .take((3 - npUsage.nps.size).coerceAtLeast(0))
        val remainder = (nonUnknownCards - selectedCards) + (cards - nonUnknownCards)
        return selectedCards + remainder
    }

    private fun isBraveChainAllowed (
        braveChainEnum: BraveChainEnum,
        npUsage: NPUsage = NPUsage.none,
        cardsPerFieldSlotMap: Map<FieldSlot, Int>
    ): Boolean {
        if (
            braveChainEnum == BraveChainEnum.Avoid
            || cardsPerFieldSlotMap.values.isEmpty()
        ) return false

        if (npUsage.nps.size == 1) {
            // Permit brave chain with NP only
            val npFieldSlot = npUsage.nps.firstOrNull()?.toFieldSlot()
            if (npFieldSlot != null) {
                return cardsPerFieldSlotMap.getOrElse(npFieldSlot) { 0 } >= 3
            }
        }

        // Sort by most number of cards
        val highestTotalCardCount = cardsPerFieldSlotMap.values.sorted().reversed().first()

        return highestTotalCardCount >= 3
    }

}