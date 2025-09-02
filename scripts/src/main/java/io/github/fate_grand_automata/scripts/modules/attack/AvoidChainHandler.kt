package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.set

@ScriptScope
class AvoidChainHandler @Inject constructor(
    private val utils: Utils
) {
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
        // No matter what is returned, it will be valid for sure
        if (npUsage.nps.size == 2) {
            // No matter what is returned, it will be valid for sure
            if (!avoidCardChains) return cards
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

        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: utils.getCardsPerFieldSlotMap(cards, npUsage)
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: utils.getCardsPerCardTypeMap(cards, npTypes)

        // Otherwise, calculate
        val cardsNeeded = 3 - npUsage.nps.size
        val selectedCards = mutableListOf<ParsedCard>()
        for (card in cards) {
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

            // If reach here, card can be added
            selectedCards.add(card)
            selectedFieldSlots[fieldSlot] = fieldSlotCount + 1
            selectedCardTypes[cardType] = cardTypeCount + 1

            // If goal has been reached, end the loop
            if (selectedCards.size >= cardsNeeded) break
        }

        // Return empty if cannot fulfill
        if (selectedCards.size < cardsNeeded) return null

        val remainder = cards - selectedCards
        val combinedCards = selectedCards + remainder

        return combinedCards
    }
}