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
class BraveChainHandler @Inject constructor() {
    fun isBraveChainAllowed (
        braveChainEnum: BraveChainEnum,
        cards: List<ParsedCard>,
        npUsage: NPUsage
    ): Boolean {
        val cardsPerFieldSlotMap = getCardsPerFieldSlotMap(cards, npUsage)
        return isBraveChainAllowed(braveChainEnum, cardsPerFieldSlotMap)
    }

    fun pick(
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        npUsage: NPUsage = NPUsage.none,
    ): List<ParsedCard>? {
        val cardsPerFieldSlotMap = getCardsPerFieldSlotMap(cards, npUsage)
        if (!isBraveChainAllowed(braveChainEnum, cardsPerFieldSlotMap)) return null

        val braveChainCapableFieldSlots = getFieldSlotsWithValidBraveChain(cardsPerFieldSlotMap)

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

    private fun getCardsPerFieldSlotMap (
        cards: List<ParsedCard>,
        npUsage: NPUsage
    ): Map<FieldSlot, Int> {
        // Card list check
        val cardsPerFieldSlotMap: MutableMap<FieldSlot, Int> = mutableMapOf()
        for (card in cards) {
            val fieldSlot = card.fieldSlot
            if (fieldSlot == null) continue
            val currentValue = cardsPerFieldSlotMap.getOrElse(fieldSlot) { 0 }
            cardsPerFieldSlotMap[fieldSlot] = currentValue + 1
        }
        // NPs check
        // Any more than 1 NP means that it is impossible to Brave Chain
        // 0 NPs does not need handling
        if (npUsage.nps.size == 1) {
            val fieldSlot = npUsage.nps.first().toFieldSlot()
            val currentValue = cardsPerFieldSlotMap.getOrElse(fieldSlot) { 0 }
            cardsPerFieldSlotMap[fieldSlot] = currentValue + 1
        }
        return cardsPerFieldSlotMap.toMap()
    }

    fun getFieldSlotsWithValidBraveChain (
        cards: List<ParsedCard>,
        npUsage: NPUsage
    ): List<FieldSlot> {
        val cardsPerFieldSlotMap = getCardsPerFieldSlotMap(cards, npUsage)
        return getFieldSlotsWithValidBraveChain(cardsPerFieldSlotMap)
    }

    private fun getFieldSlotsWithValidBraveChain (
        cardsPerFieldSlotMap: Map<FieldSlot, Int>
    ): List<FieldSlot> {
        return cardsPerFieldSlotMap.keys
            .filter { cardsPerFieldSlotMap.getOrElse(it) { 0 } >= 3 }
            .map { it }
    }

}