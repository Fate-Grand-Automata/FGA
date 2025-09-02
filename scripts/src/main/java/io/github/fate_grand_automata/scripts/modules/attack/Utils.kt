package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class Utils @Inject constructor() {
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

    fun getBraveChainFieldSlot (
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        npUsage: NPUsage = NPUsage.none,
        forceBraveChain: Boolean = false,
    ): FieldSlot? {
        return if (braveChainEnum == BraveChainEnum.Avoid) null
        else if (npUsage.nps.size == 1) {
            // Get np if there is only 1 (since we want to try for Brave Chain)
            val firstNp = npUsage.nps.firstOrNull()
            firstNp?.toFieldSlot()
        } else if (braveChainEnum == BraveChainEnum.Always || forceBraveChain) {
            // Force brave chain only if it always wants a Brave Chain
            val braveChainCapableFieldSlots = getFieldSlotsWithValidBraveChain(cards, npUsage)
            val braveChainPriorityCard = cards.firstOrNull { braveChainCapableFieldSlots.contains(it.fieldSlot) }
            // Return the first valid one, since it is already the highest priority
            braveChainPriorityCard?.fieldSlot
        } else {
            null
        }
    }

    fun List<ChainTypeEnum>.isAfterAvoid(chainTypeEnum: ChainTypeEnum, indexOfAvoid: Int? = null): Boolean {
        val avoidIndex = indexOfAvoid ?: this.indexOf(ChainTypeEnum.Avoid)
        val enumIndex = this.indexOf(chainTypeEnum)
        if (avoidIndex < 0) return false
        return enumIndex > avoidIndex
    }
}