package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class BraveChainHandler @Inject constructor(
    private val utils: Utils
) {
    fun pick(
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        npUsage: NPUsage = NPUsage.none,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardsPerFieldSlotMap = cardCountPerFieldSlotMap ?: utils.getCardsPerFieldSlotMap(cards, npUsage)
        if (!isBraveChainAllowed(braveChainEnum, cardsPerFieldSlotMap)) return null

        val braveChainCapableFieldSlots = utils.getFieldSlotsWithValidBraveChain(cardsPerFieldSlotMap)

        // Use the first valid one, since it is already the highest priority
        val braveChainPriorityCard = cards.firstOrNull { braveChainCapableFieldSlots.contains(it.fieldSlot) }
        val priorityFieldSlot = braveChainPriorityCard?.fieldSlot

        // Safety check
        if (priorityFieldSlot == null) return null
        val selectedCards = cards
            .filter { it.fieldSlot == priorityFieldSlot }
            .take((3 - npUsage.nps.size).coerceAtLeast(0))
        val remainder = cards - selectedCards
        return selectedCards + remainder
    }

    private fun isBraveChainAllowed (
        braveChainEnum: BraveChainEnum,
        cardsPerFieldSlotMap: Map<FieldSlot, Int>
    ): Boolean {
        if (
            braveChainEnum == BraveChainEnum.Avoid
            || cardsPerFieldSlotMap.values.isEmpty()
        ) return false

        // Sort by most number of cards
        val highestTotalCardCount = cardsPerFieldSlotMap.values.sorted().reversed().first()

        return highestTotalCardCount >= 3
    }

}