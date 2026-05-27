package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot

object AttackUtils {
    fun getCardsPerFieldSlotMap (
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none
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
    fun getCardsPerCardTypeMap (
        cards: List<ParsedCard>,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): Map<CardTypeEnum, Int> {
        // Card list check
        val cardsPerCardTypeMap: MutableMap<CardTypeEnum, Int> = mutableMapOf()
        for (card in cards) {
            val cardType = card.type
            val currentValue = cardsPerCardTypeMap.getOrElse(cardType) { 0 }
            cardsPerCardTypeMap[cardType] = currentValue + 1
        }
        // NPs check
        for (npType in npTypes.values) {
            val currentValue = cardsPerCardTypeMap.getOrElse(npType) { 0 }
            cardsPerCardTypeMap[npType] = currentValue + 1
        }
        return cardsPerCardTypeMap.toMap()
    }

    fun getFieldSlotsWithValidBraveChain (
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none
    ): List<FieldSlot> {
        val cardsPerFieldSlotMap = getCardsPerFieldSlotMap(cards, npUsage)
        return getFieldSlotsWithValidBraveChain(cardsPerFieldSlotMap)
    }

    fun getFieldSlotsWithValidBraveChain (
        cardsPerFieldSlotMap: Map<FieldSlot, Int>
    ): List<FieldSlot> {
        return cardsPerFieldSlotMap.keys
            .filter { cardsPerFieldSlotMap.getOrElse(it) { 0 } >= 3 }
            .map { it }
    }

    /**
     * Returns a field slot for a BraveChain if there is one available
     * @param braveChainEnum BraveChainEnum that determines the behavior of this function.
     * BraveChainEnum.None -> Does not return a fieldSlot, since BraveChains are not forced.
     * BraveChainEnum.Avoid -> Does not return a fieldSlot, since BraveChains are avoided.
     * BraveChainEnum.WithNP -> Returns a field slot if there is 1 NP with a valid BraveChain.
     * BraveChainEnum.Always -> Always returns a fieldSlot if there is a valid one.
     * @param cards The list of cards available to choose from
     * @param npUsage NPs that have been clicked
     * @returns A valid FieldSlot for a BraveChain or null if there is none
     */
    fun getBraveChainFieldSlot (
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
    ): FieldSlot? {
        if (
            braveChainEnum == BraveChainEnum.Avoid
            || braveChainEnum == BraveChainEnum.None
            // Since it is impossible to make a BraveChain
            || npUsage.nps.size > 1) return null
        else if (npUsage.nps.size == 1) {
            // Get np if there is only 1 (since we want to try for Brave Chain)
            val firstNp = npUsage.nps.firstOrNull()
            val fieldSlot = firstNp?.toFieldSlot()
            // Only return the field slot if it is valid for a Brave Chain
            if (cards.filter { it.fieldSlot == fieldSlot }.size >= 2) return fieldSlot
        } else if (braveChainEnum == BraveChainEnum.Always) {
            // Force brave chain only if it always wants a Brave Chain
            val braveChainCapableFieldSlots = getFieldSlotsWithValidBraveChain(cards, npUsage)
            val braveChainPriorityCard = cards.firstOrNull { braveChainCapableFieldSlots.contains(it.fieldSlot) }
            // Return the first valid one, since it is already the highest priority
            return braveChainPriorityCard?.fieldSlot
        }
        return null
    }

    /**
     * @returns A list of non-Unknown type cards that can be used to make a Chain.
     */
    fun getValidNonUnknownCards (
        cards: List<ParsedCard>
    ): List<ParsedCard> {
        // Try to ensure unknown is handled
        return cards.filter { it.type != CardTypeEnum.Unknown }
    }

    /**
     * @returns Are there enough cards to make a chain?
     */
    fun isChainable (
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
    ): Boolean {
        if (npTypes.values.contains(CardTypeEnum.Unknown)) return false
        val cardsNeeded = 3 - npUsage.nps.size
        return getValidNonUnknownCards(cards).size >= cardsNeeded
    }
}