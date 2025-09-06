package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class BraveChainHandler @Inject constructor(
    private val utils: AttackUtils,
    private val avoidChainHandler: AvoidChainHandler,
) {
    fun pick(
        npUsage: NPUsage = NPUsage.none,
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        // Try to ensure unknown is handled
        val filteredCards = utils.getValidNonUnknownCards(cards)
        if (!utils.isChainable(
            cards = filteredCards,
            npUsage = npUsage,
        )) {
            return null
        }

        val cardsPerFieldSlotMap = cardCountPerFieldSlotMap ?: utils.getCardsPerFieldSlotMap(filteredCards, npUsage)

        if (braveChainEnum == BraveChainEnum.Avoid) {
            return avoidChainHandler.pick(
                cards = filteredCards,
                npUsage = npUsage,
                avoidBraveChains = true,
                avoidCardChains = false,
            )
        }

        if (!isBraveChainAllowed(braveChainEnum, npUsage, cardsPerFieldSlotMap)) return null

        // Always returns a valid field slot if there is one for BraveChain
        val priorityFieldSlot = utils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = filteredCards,
            npUsage = npUsage,
        )
        if (priorityFieldSlot == null) return null

        val selectedCards = filteredCards
            .filter { it.fieldSlot == priorityFieldSlot }
            .take((3 - npUsage.nps.size).coerceAtLeast(0))
        val remainder = filteredCards - selectedCards
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