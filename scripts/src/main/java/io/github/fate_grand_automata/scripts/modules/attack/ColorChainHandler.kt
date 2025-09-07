package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.Map

@ScriptScope
open class ColorChainHandler @Inject constructor() {
    // Returns null if uniqueCards cannot be found
    fun pick (
        cardType: CardTypeEnum,
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        forceBraveChain: Boolean = false,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
    ): List<ParsedCard>? {
        // NEVER want to make a chain of Unknown cards
        if (cardType == CardTypeEnum.Unknown) return null

        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(cards, npTypes)
        if (!isColorChainAllowed(cardType, cards, npTypes, cardCountPerCardTypeMap)) return null

        // Check for Brave Chain
        val braveChainFieldSlot = AttackUtils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = cards,
            npUsage = npUsage,
            forceBraveChain = forceBraveChain,
        )
        val braveChainEnum = if (forceBraveChain) BraveChainEnum.Always else braveChainEnum

        // if it passes the above, the actual implementation is very easy
        val cardsNeeded = 3 - npUsage.nps.size
        var selectedCards = cards.filter { it.type == cardType }

        // Do a pre-emptive check
        if (!AttackUtils.isChainable(
            cards = selectedCards,
            npUsage = npUsage,
            npTypes = npTypes,
        )) {
            return null
        }

        // Do not have any BraveChains or it is invalid
        if (braveChainEnum == BraveChainEnum.Avoid) {
            val newSelection = getCardsForAvoidBraveChain(
                cards = cards,
                selectedCards = selectedCards,
                npUsage = npUsage,
                cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
            )
            // if it is null, it means that it is impossible to get a list that avoids a BraveChain
            // if it is not null, there is a valid non-Brave Chain option
            if (newSelection == null) return null
            selectedCards = newSelection.toMutableList()
        }
        // If there is a braveChainFieldSlot, try for a Brave Color Chain
        else if (braveChainFieldSlot != null) {
            // Attempt to find one matching the fieldSlot
            val fieldSlotList = selectedCards.filter {
                it.fieldSlot == braveChainFieldSlot
            }
            // If there is a valid number of cards for the fieldSlot, attempt to Brave Chain
            // Even if it is not valid, if forceBraveChain is on,
            // it only accepts Brave Color Chains and not normal Color Chains
            if (fieldSlotList.size >= cardsNeeded || braveChainEnum == BraveChainEnum.Always)
                selectedCards = fieldSlotList
        }

        // Secondary check
        if (selectedCards.size < cardsNeeded) return null
        val combinedCards = selectedCards + (cards - selectedCards)
        return combinedCards
    }

    fun isColorChainAllowed (
        cardType: CardTypeEnum,
        cards: List<ParsedCard>,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null
    ): Boolean {
        // NEVER want to make a chain of Unknown cards
        if (cardType == CardTypeEnum.Unknown) return false

        val uniqueCardTypesFromNp = npTypes.values.toSet()
        // Impossible if the unique number of card types are not 1
        if (uniqueCardTypesFromNp.size > 1) return false
        // Impossible if the nps are not of the chain type
        if (uniqueCardTypesFromNp.size == 1 && uniqueCardTypesFromNp.firstOrNull() != cardType) return false

        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(cards, npTypes)

        // Ensure there are at least 3 cards of the type
        // This innately handles Unknown for Color chain
        return cardCountPerCardTypeMap.getOrElse(cardType) { 0 } >= 3
    }

    /**
     * Tries to get a list of valid cards for BraveChainEnum.Avoid while fulfilling the color requirement of this Chain.
     *
     * @param cards The list of cards available to choose from
     * @param npUsage NPs that have been clicked
     * @param selectedCards The list of cards that fulfil the color chain
     * @param cardCountPerFieldSlotMap Optional passing of map from parent to reuse. Otherwise, this function will generate its own.
     * @return null if there is no valid selectable options, otherwise a valid list of ParsedCards.
     */
    fun getCardsForAvoidBraveChain (
        cards: List<ParsedCard>,
        selectedCards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardsNeeded = 3 - npUsage.nps.size
        if (selectedCards.size < cardsNeeded) return null

        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: AttackUtils.getCardsPerFieldSlotMap(cards, npUsage)
        // If there is only 1 unique field slot throughout, this is a valid entry (since it is impossible to avoid Brave Chain)
        if (cardCountPerFieldSlotMap.size == 1) return selectedCards

        val npFieldSlots = npUsage.nps.map { it.toFieldSlot() }.toSet()
        // If there is 2 or more NP, it will never be a BraveChain, so any cards are valid
        if (npFieldSlots.size >= 2) return selectedCards

        val selectedCardsFieldSlotSet = selectedCards.mapNotNull { it.fieldSlot }.toSet()
        // If the cards available are not 1 fieldSlot, but the selected cards are 1 fieldSlot only
        if (selectedCardsFieldSlotSet.size == 1) return null // invalid

        val npFieldSlot = npFieldSlots.firstOrNull()

        // Otherwise, a valid list of cards exist, but the correct cards must be selected
        val filteredCards = selectedCards.take(cardsNeeded).toMutableList()
        val filteredCardsSet = filteredCards.mapNotNull { it.fieldSlot }.toSet()
        if (
        // If all cards are the same field slot
            filteredCardsSet.size == 1
            // it is the same slot as the NP
            && (npFieldSlot == filteredCardsSet.first()
                // or the NP is null
                || npFieldSlot == null
            )
        ) {
            // Attempt to fetch a different field slot
            val differentCard = cards.firstOrNull() { it.fieldSlot != filteredCardsSet.first() }
            // If there is no different card (even though there should, by this stage), return null
            if (differentCard == null) return null
            // Just add new card to index 1, aka 2nd card
            filteredCards.add(1, differentCard)
        }
        return filteredCards + (cards - filteredCards)
    }
}