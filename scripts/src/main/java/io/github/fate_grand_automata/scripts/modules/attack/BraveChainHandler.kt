package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class BraveChainHandler @Inject constructor(
    private val utils: AttackUtils
) {
    fun pick(
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
        npUsage: NPUsage = NPUsage.none,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardsPerFieldSlotMap = cardCountPerFieldSlotMap ?: utils.getCardsPerFieldSlotMap(cards, npUsage)
        if (!isBraveChainAllowed(braveChainEnum, npUsage, cardsPerFieldSlotMap)) return null

        val braveChainCapableFieldSlots = utils.getFieldSlotsWithValidBraveChain(cardsPerFieldSlotMap)

        val npFieldSlot = if (npUsage.nps.size == 1) npUsage.nps.firstOrNull()?.toFieldSlot() else null

        val braveChainPriorityCard = cards.firstOrNull {
            // Use the first valid one, since it is already the highest priority
            braveChainCapableFieldSlots.contains(it.fieldSlot) &&
            // And if there is an NP, it must match the NP
            (npFieldSlot == null || npFieldSlot == it.fieldSlot)
        }
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