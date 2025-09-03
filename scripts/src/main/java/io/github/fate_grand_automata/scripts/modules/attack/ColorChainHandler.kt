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
open class ColorChainHandler @Inject constructor(
    private val utils: Utils
) {
    // Returns null if uniqueCards cannot be found
    fun pick (
        cardType: CardTypeEnum,
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        forceBraveChain: Boolean = false,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null
    ): List<ParsedCard>? {
        // NEVER want to make a chain of Unknown cards
        if (cardType == CardTypeEnum.Unknown) return null

        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: utils.getCardsPerCardTypeMap(cards, npTypes)
        if (!isColorChainAllowed(cardType, cards, npTypes, cardCountPerCardTypeMap)) return null

        // Check for Brave Chain
        val braveChainFieldSlot = utils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = cards,
            npUsage = npUsage,
            forceBraveChain = forceBraveChain,
        )
        val braveChainEnum = if (forceBraveChain) BraveChainEnum.Always else braveChainEnum

        // if it passes the above, the actual implementation is very easy
        val cardsNeeded = 3 - npUsage.nps.size
        var selectedCards = cards.filter { it.type == cardType }

        // Do not have any BraveChains or it is invalid
        if (braveChainEnum == BraveChainEnum.Avoid) {
            val newSelection = utils.getCardsForAvoidBraveChain(
                cards = selectedCards,
                npUsage = npUsage,
            )
            if (newSelection == null) return null
            selectedCards = newSelection
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
        val uniqueCardTypesFromNp = npTypes.values.toSet()
        // Impossible if the unique number of card types are not 1
        if (uniqueCardTypesFromNp.size > 1) return false
        // Impossible if the nps are not of the chain type
        if (uniqueCardTypesFromNp.size == 1 && uniqueCardTypesFromNp.firstOrNull() != cardType) return false

        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: utils.getCardsPerCardTypeMap(cards, npTypes)

        // Ensure there are at least 3 cards of the type
        return cardCountPerCardTypeMap.getOrElse(cardType) { 0 } >= 3
    }
}