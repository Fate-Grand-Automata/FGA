package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.set

object AvoidChainHandler {
    fun pick(
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        avoidBraveChains: Boolean = true,
        avoidCardChains: Boolean = true,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
    ): List<ParsedCard>? {
        // If there are already 3 NPs, there is nothing to avoid
        if (npUsage.nps.size == 3) return null

        // Try to ensure unknown is handled
        val nonUnknownCards = AttackUtils.getValidNonUnknownCards(cards)
        if (!AttackUtils.isChainable(
            cards = nonUnknownCards,
            npUsage = npUsage,
            npTypes = npTypes,
        )) return null

        if (npUsage.nps.size == 2) {
            // No matter what is returned, it will be valid for sure
            if (!avoidCardChains) return nonUnknownCards + (cards - nonUnknownCards)
            // Otherwise, need to check if they have the same NP type
        }

        // For tracking
        val selectedFieldSlots = mutableMapOf<FieldSlot, Int>()
        val selectedCardTypes = mutableMapOf<CardTypeEnum, Int>()

        if (avoidBraveChains) {
            for (np in npUsage.nps) {
                val fieldSlot = np.toFieldSlot()
                val currentValue = selectedFieldSlots.getOrElse(fieldSlot) { 0 }
                selectedFieldSlots[fieldSlot] = currentValue + 1
            }
        }
        if (avoidCardChains) {
            for (npType in npTypes.values) {
                val currentValue = selectedCardTypes.getOrElse(npType) { 0 }
                selectedCardTypes[npType] = currentValue + 1
            }
        }

        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: AttackUtils.getCardsPerFieldSlotMap(nonUnknownCards, npUsage)
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(nonUnknownCards, npTypes)

        var cachedFilteredCards: List<ParsedCard> = nonUnknownCards
        var previousFieldSlot: FieldSlot? = null

        // Otherwise, calculate
        val cardsNeeded = 3 - npUsage.nps.size
        val selectedCards = mutableListOf<ParsedCard>()
        while (selectedCards.size < cardsNeeded) {
            // Loop through all the cards at least one time so that the next pass is easier
            var filteredCards = mutableListOf<ParsedCard>()
            for (card in cachedFilteredCards) {
                if (card in selectedCards) continue
                val fieldSlot = card.fieldSlot
                if (fieldSlot == null) continue
                val fieldSlotCount = selectedFieldSlots.getOrElse(fieldSlot) { 0 }
                if (avoidBraveChains
                    && (
                        // If there are already 2 selected cards with the same card slot
                        fieldSlotCount >= 2
                        // and there are still valid targets, skip
                        && cardCountPerFieldSlotMap.size > 1
                    )
                ) continue

                val cardType = card.type
                val cardTypeCount = selectedCardTypes.getOrElse(cardType) { 0 }
                if (avoidCardChains
                    // There are at least 2 cards to choose from
                    && cardCountPerCardTypeMap.size > 1
                    && (
                        cardTypeCount >= 2
                        // Or if it would make a Mighty Chain
                        || (cardTypeCount == 0 && selectedCardTypes.size >= 2)
                    )
                ) continue
                // If reach here, add to filteredList
                filteredCards.add(card)
            }

            // Cache the filteredCards to make the next check faster
            cachedFilteredCards = filteredCards

            // Try to make the next card different from the previous card's fieldSlot, if possible
            if (previousFieldSlot != null) {
                val fieldSlotFilter = filteredCards.filter {
                    it.fieldSlot != previousFieldSlot
                }
                if (fieldSlotFilter.isNotEmpty()) filteredCards = fieldSlotFilter.toMutableList()
            }

            val filteredCard = filteredCards.firstOrNull()
            if (filteredCard == null || filteredCard.fieldSlot == null) break // If cannot find, leave

            // If reach here, card can be added
            selectedCards.add(filteredCard)
            selectedFieldSlots[filteredCard.fieldSlot] = selectedFieldSlots.getOrElse(filteredCard.fieldSlot) { 0 } + 1
            selectedCardTypes[filteredCard.type] = selectedCardTypes.getOrElse(filteredCard.type) { 0 } + 1
            previousFieldSlot = filteredCard.fieldSlot
        }

        // Return empty if cannot fulfill
        if (selectedCards.size < cardsNeeded) return null

        val remainder = (nonUnknownCards - selectedCards) + (cards - nonUnknownCards)
        val combinedCards = selectedCards + remainder

        return combinedCards
    }
}