package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
object MightyChainHandler {
    private const val MIGHTY_CHAIN_CARD_TYPE_COUNT = 3

    // Returns null if uniqueCards cannot be found
    fun pick(
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
        forceBraveChain: Boolean = false,
        requiredFieldSlot: FieldSlot? = null,
    ): List<ParsedCard>? {
        val nonUnknownCards = AttackUtils.getValidNonUnknownCards(cards)
        if (
            !AttackUtils.isChainable(
                cards = nonUnknownCards,
                npUsage = npUsage,
                npTypes = npTypes,
            )
        ) {
            return null
        }

        val uniqueCardTypesFromNp = npTypes.values.toSet()
        val cardCountsByType = cardCountPerCardTypeMap
            ?: AttackUtils.getCardsPerCardTypeMap(nonUnknownCards, npTypes)
        if (
            !isMightyChainAllowed(
                cards = nonUnknownCards,
                npUsage = npUsage,
                uniqueCardTypesFromNp = uniqueCardTypesFromNp,
                npTypes = npTypes,
                cardCountPerCardTypeMap = cardCountsByType,
            )
        ) return null

        val braveChainFieldSlot = requiredFieldSlot ?: AttackUtils.getBraveChainFieldSlot(
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
            forceBraveChain = forceBraveChain || requiredFieldSlot != null,

            cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
        )

        return selectedCards?.let { it + (cards - it) }
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
        forceBraveChain: Boolean = false,

        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardsToFind = MIGHTY_CHAIN_CARD_TYPE_COUNT
        val uniqueCardTypes = uniqueCardTypesAlreadyFilled.toMutableSet()

        var selectedCards = mutableListOf<ParsedCard>()
        var cachedFilteredCards: List<ParsedCard> = cards
        while (uniqueCardTypes.size < cardsToFind) {
            var filteredCards = cachedFilteredCards.filter {
                it.type !in uniqueCardTypes
                    && it !in selectedCards
            }
            cachedFilteredCards = filteredCards

            // If there is a braveChainFieldSlot, we want to try for a Brave Mighty Chain
            if (braveChainFieldSlot != null && braveChainEnum != BraveChainEnum.Avoid) {
                val fieldSlotList = filteredCards.filter {
                    it.fieldSlot == braveChainFieldSlot
                }
                // A required Brave Mighty candidate must stay on that servant.
                if (fieldSlotList.isNotEmpty() || forceBraveChain) filteredCards = fieldSlotList
            }
            val filteredCard = filteredCards.firstOrNull()
            if (filteredCard == null) break
            uniqueCardTypes.add(filteredCard.type)
            selectedCards.add(filteredCard)
        }

        if (uniqueCardTypes.size < cardsToFind) return null

        if (braveChainEnum == BraveChainEnum.Avoid) {
            val newSelection = getMightyChainWithoutBraveChain(
                cards = cards,
                npUsage = npUsage,
                selectedCards = selectedCards.toList(),
                cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
            )
            if (newSelection == null) return null
            selectedCards = newSelection.toMutableList()
        }

        val remainder = cards - selectedCards
        return selectedCards + remainder
    }

    fun getMightyChainWithoutBraveChain(
        cards: List<ParsedCard>,
        selectedCards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
    ): List<ParsedCard>? {
        val cardCountsByFieldSlot = cardCountPerFieldSlotMap
            ?: AttackUtils.getCardsPerFieldSlotMap(cards, npUsage)
        // If there is only 1 unique field slot throughout, this is a valid entry (since it is impossible to avoid Brave Chain)
        if (cardCountsByFieldSlot.size == 1) return selectedCards

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
            val validReplacements = cards.filter {
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

    fun isMightyChainAllowed(
        cards: List<ParsedCard>,
        npUsage: NPUsage,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        uniqueCardTypesFromNp: Set<CardTypeEnum>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
    ): Boolean {
        val npCardTypes = uniqueCardTypesFromNp ?: npTypes.values.toSet()
        val cardCountsByType = cardCountPerCardTypeMap
            ?: AttackUtils.getCardsPerCardTypeMap(cards, npTypes)
        val npUsageSize = npUsage.nps.size

        // Fall back to normal card selection unless every selected NP has a known type.
        if (npUsageSize > 0 && npTypes.size != npUsageSize) return false
        if (CardTypeEnum.Unknown in npTypes.values) return false

        if (cardCountsByType.keys.count { it != CardTypeEnum.Unknown } < MIGHTY_CHAIN_CARD_TYPE_COUNT) {
            return false
        }

        // Multiple NPs can form a Mighty Chain only when none share a card type.
        return npUsageSize <= 1 || npCardTypes.size == npUsageSize
    }
}
