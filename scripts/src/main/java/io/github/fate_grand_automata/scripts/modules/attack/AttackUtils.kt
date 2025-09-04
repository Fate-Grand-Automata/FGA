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
class AttackUtils @Inject constructor() {
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
     * @param forceBraveChain Treats braveChainEnum as BraveChainEnum.Always
     * @returns A valid FieldSlot for a BraveChain or null if there is none
     */
    fun getBraveChainFieldSlot (
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        forceBraveChain: Boolean = false,
    ): FieldSlot? {
        val braveChainEnum = if (forceBraveChain) BraveChainEnum.Always else braveChainEnum
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
            if (cards.filter { it.fieldSlot == fieldSlot }.size > 1) return fieldSlot
        } else if (braveChainEnum == BraveChainEnum.Always) {
            // Force brave chain only if it always wants a Brave Chain
            val braveChainCapableFieldSlots = getFieldSlotsWithValidBraveChain(cards, npUsage)
            val braveChainPriorityCard = cards.firstOrNull { braveChainCapableFieldSlots.contains(it.fieldSlot) }
            // Return the first valid one, since it is already the highest priority
            return braveChainPriorityCard?.fieldSlot
        }
        return null
    }

    fun getCardsForAvoidBraveChain (
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
    ): List<ParsedCard>? {
        val npFieldSlots = npUsage.nps.map { it.toFieldSlot() }.toSet()
        // If there is 2 or more NP, it will never be a BraveChain
        if (npFieldSlots.size <= 1) {
            val fieldSlotSet = cards.map { it.fieldSlot }.toSet()
            if (
            // If there is only 1 unique field slot in the list
                fieldSlotSet.size == 1
                && (
                    // no NPs to cancel out
                    npFieldSlots.isEmpty()
                    // Or there is exactly 1 NP and it is the same
                    || npFieldSlots.first() == fieldSlotSet.first()
                )
            ) return null

            val npFieldSlot = npFieldSlots.firstOrNull()

            // Otherwise, it is valid, but the correct cards must be selected
            val cardsNeeded = 3 - npUsage.nps.size
            val filteredCards = cards.take(cardsNeeded).toMutableList()
            val firstFieldSlot =  filteredCards.first().fieldSlot
            if (
                // If all cards are the same field slot
                filteredCards.all { it.fieldSlot == firstFieldSlot }
                && (
                    // and it matches the NP
                    npFieldSlot == firstFieldSlot
                    // or the NP is null
                        || npFieldSlot == null
                )
            ) {
                val differentSlot = cards.firstOrNull() { it.fieldSlot != firstFieldSlot }
                if (differentSlot == null) return null
                // Just add new card to index 1, aka 2nd card
                filteredCards.add(1, differentSlot)
            }
            return filteredCards + (cards - filteredCards)
        }

        return cards
    }
}