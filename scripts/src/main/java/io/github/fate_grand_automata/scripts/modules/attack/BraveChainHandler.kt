package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot

object BraveChainHandler {
    fun pick(
        npUsage: NPUsage = NPUsage.none,
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum,
    ): List<ParsedCard>? {
        if (braveChainEnum == BraveChainEnum.Avoid) {
            return AvoidChainHandler.pick(
                cards = cards,
                npUsage = npUsage,
                braveChainEnum = braveChainEnum,
                avoidCardChains = false,
            )
        }

        // Prefer readable cards when there is a choice, and keep stunned cards at the end of the hand.
        val orderedCards = cards.sortedWith(
            compareBy<ParsedCard> { it.isStunned }.thenBy { it.type == CardTypeEnum.Unknown }
        )
        // A Brave Chain only needs the Servant's slot. Count every selectable card with a known slot,
        // including unreadable colors, and let the shared count include a single NP by its slot.
        val usableCards = orderedCards.filter { !it.isStunned && it.fieldSlot != null }

        val cardsPerFieldSlotMap = AttackUtils.getCardsPerFieldSlotMap(usableCards, npUsage)
        if (!isBraveChainAllowed(braveChainEnum, npUsage, cardsPerFieldSlotMap)) return null

        val priorityFieldSlot = AttackUtils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = usableCards,
            npUsage = npUsage,
        )
        if (priorityFieldSlot == null) return null

        // Keep spare playable cards from the same Servant next in line so they can complete
        // a face-card Brave Chain if the planned NP fails to activate.
        val (braveCards, remainingCards) = orderedCards.partition {
            !it.isStunned && it.fieldSlot == priorityFieldSlot
        }
        return braveCards + remainingCards
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
