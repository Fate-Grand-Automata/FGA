package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import kotlin.collections.Map

object MightyChainHandler {
    // We want 3 unique types, the magic number
    const val TOTAL_UNIQUE_CARD_TYPES_PERMITTED = 3

    // Returns null if uniqueCards cannot be found
    fun pick (
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
    ): List<ParsedCard>? {
        // Try to ensure unknown is handled
        val nonUnknownCards = AttackUtils.getValidNonUnknownCards(cards)
        if (!AttackUtils.isChainable(
            cards = nonUnknownCards,
            npUsage = npUsage,
            npTypes = npTypes,
        )) {
            return null
        }

        val uniqueCardTypesFromNp = npTypes.values.toSet()
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(nonUnknownCards, npTypes)
        if (!isMightyChainAllowed(
            cards = nonUnknownCards,
            npUsage = npUsage,
            uniqueCardTypesFromNp = uniqueCardTypesFromNp,
            npTypes = npTypes,
            cardCountPerCardTypeMap = cardCountPerCardTypeMap
        )) return null

        // Check for Brave Chain
        val braveChainFieldSlot = AttackUtils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = nonUnknownCards,
            npUsage = npUsage,
        )

        val selectedCards = pick(
            cards = nonUnknownCards,
            npUsage = npUsage,
            uniqueCardTypesAlreadyFilled = uniqueCardTypesFromNp,
            braveChainFieldSlot = braveChainFieldSlot,
            braveChainEnum = braveChainEnum,

            cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
        )

        if (selectedCards == null) return null
        // Handle for CardType.Unknown and ensure that 5 cards are always returned
        return selectedCards + (cards - selectedCards)
    }

    // Returns null if uniqueCards cannot be found
    private fun pick(
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        // Decreases number of cards to get
        // e.g. usually based on npSize
        uniqueCardTypesAlreadyFilled: Set<CardTypeEnum>,
        // In case of a Brave chain, we want to know what slot it is
        braveChainFieldSlot: FieldSlot? = null,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,

        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardsToFind = TOTAL_UNIQUE_CARD_TYPES_PERMITTED
        val uniqueCardTypes = uniqueCardTypesAlreadyFilled.toMutableSet()

        var selectedCards = mutableListOf<ParsedCard>()
        // Cache the filteredList the first time, to make the next check faster
        var cachedFilteredCards: List<ParsedCard> = cards
        while (uniqueCardTypes.size < cardsToFind) {
            var filteredCards = cachedFilteredCards.filter {
                // Always look for a different card type
                it.type !in uniqueCardTypes
                // It should never reach here due to needing uniqueCardTypes, but just in case
                && it !in selectedCards
            }
            cachedFilteredCards = filteredCards

            // If there is a braveChainFieldSlot, we want to try for a Brave Mighty Chain
            if (braveChainFieldSlot != null && braveChainEnum != BraveChainEnum.Avoid) {
                // Attempt to find one matching the fieldSlot
                val fieldSlotList = filteredCards.filter {
                    it.fieldSlot == braveChainFieldSlot
                }
                // Even if it is empty, if BraveChainEnum.Always,
                // it only accepts Brave Mighty Chains and not normal Mighty Chains
                if (fieldSlotList.isNotEmpty() || braveChainEnum == BraveChainEnum.Always) filteredCards = fieldSlotList
            }
            val filteredCard = filteredCards.firstOrNull()
            if (filteredCard == null) break // If cannot find, leave
            uniqueCardTypes.add(filteredCard.type)
            selectedCards.add(filteredCard)
        }

        // If there isn't a valid list of cards, reject and return null
        if (uniqueCardTypes.size < cardsToFind) return null

        if (braveChainEnum == BraveChainEnum.Avoid) {
            val newSelection = getMightyChainWithoutBraveChain(
                cards = cards,
                npUsage = npUsage,
                selectedCards = selectedCards.toList(),
                cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
            )
            if (newSelection == null) return null
            // if it is not null, there is a valid non-Brave Chain option
            selectedCards = newSelection.toMutableList()
        }

        // Otherwise, we return the expected output
        val remainder = cards - selectedCards
        val combinedCards = selectedCards + remainder

        return combinedCards
    }

    fun getMightyChainWithoutBraveChain (
        cards: List<ParsedCard>,
        selectedCards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: AttackUtils.getCardsPerFieldSlotMap(cards, npUsage)
        // If there is only 1 unique field slot throughout, this is a valid entry (since it is impossible to avoid Brave Chain)
        if (cardCountPerFieldSlotMap.size == 1) return selectedCards

        val allFieldSlots = selectedCards.mapNotNull { it.fieldSlot } + npUsage.nps.map { it.toFieldSlot() }
        val uniqueFieldSlotsSet = allFieldSlots.toSet()
        // If there is more than 1 unique fieldSlot, returning is fine
        if (uniqueFieldSlotsSet.size > 1) return selectedCards

        // Otherwise, attempt to substitute cards
        val newSelection = mutableListOf<ParsedCard>()
        var isNewSelectionValid = false
        for (card in selectedCards) {
            // if already valid, add the card and continue
            if (isNewSelectionValid) {
                newSelection.add(card)
                continue
            }
            val validReplacements = cards.filter{
                it.type == card.type
                && it.fieldSlot != card.fieldSlot
            }
            if (validReplacements.isEmpty()) {
                newSelection.add(card)
            } else {
                // Found a suitable replacement
                newSelection.add(validReplacements.first())
                isNewSelectionValid = true
            }
        }

        // If a suitable replacement was found, return is allowed
        if (isNewSelectionValid) {
            return newSelection
        }

        // Otherwise, return null
        return null
    }

    fun isMightyChainAllowed (
        cards: List<ParsedCard>,
        npUsage: NPUsage,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        uniqueCardTypesFromNp: Set<CardTypeEnum>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
    ): Boolean {
        val uniqueCardTypesFromNp = uniqueCardTypesFromNp ?: npTypes.values.toSet()
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(cards, npTypes)
        val npUsageSize = npUsage.nps.size

        // Unable to make a Mighty Chain with Unknown cards in NP
        if (npTypes.values.contains(CardTypeEnum.Unknown)) return false

        // if there are more than 1 NP and we don't know their types,
        // just do the default since we can't guarantee a mighty chain anyway
        if (npUsageSize > 0 && npTypes.size != npUsageSize) return false

        // Unable to make a Mighty Chain with Unknown cards in NP
        if (npTypes.values.contains(CardTypeEnum.Unknown)) return false

        // Do not accept Unknown cards for Mighty Chain
        if (cardCountPerCardTypeMap.getOrElse(CardTypeEnum.Unknown) { 0 } > 0) return false

        // If we do not have 3 unique cards at least, it is impossible to Mighty Chain
        if (cardCountPerCardTypeMap.size < 3) return false

        // Success if
        return (
                // number of nps is 1
                npUsageSize == 1
                // or number of nps do not match with unique size
                // scenarios:
                // 1 NP = no problem, we proceed
                // 3 NP = Even if it doesn't match, it doesn't matter to us since all are filled
                // 2 NP (different) = 2 unique == 2 nps (clear to proceed)
                // 2 NP (same) = 2 clash already. No mighty chain here
                || uniqueCardTypesFromNp.size == npUsageSize
        )
    }
}