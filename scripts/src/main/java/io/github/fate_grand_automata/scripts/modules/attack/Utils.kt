package io.github.fate_grand_automata.scripts.modules.attack

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
}