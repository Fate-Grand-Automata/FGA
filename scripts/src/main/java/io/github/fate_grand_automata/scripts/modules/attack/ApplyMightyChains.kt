package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ApplyMightyChains @Inject constructor() {
    // We want 3 unique types, the magic number
    val totalUniqueCardTypesPermitted = 3

    fun getMightyChain (
        cards: List<ParsedCard>,
        npUsage: NPUsage,
        npTypes: Map<FieldSlot, CardTypeEnum>,
        chainPriority: List<ChainTypeEnum>? = null
    ): List<ParsedCard>? {
        val uniqueCardTypesFromNp = npTypes.values.toSet()
        if (!isMightyChainAllowed(npUsage, uniqueCardTypesFromNp, npTypes)) return null

        // Get np if there is only 1 (since we want to try for Brave Chain)
        val firstNp = if (npUsage.nps.size > 1) npUsage.nps.firstOrNull() else null
        val firstFieldSlot = firstNp?.toFieldSlot()

        return tryToGetMightyChain(
            cards,
            uniqueCardTypesFromNp,
            firstFieldSlot,
            chainPriority
        )
    }

    // Returns null if uniqueCards cannot be found
    fun tryToGetMightyChain(
        cards: List<ParsedCard>,
        // Decreases number of cards to get
        // e.g. usually based on npSize
        uniqueCardTypesAlreadyFilled: Set<CardTypeEnum>,
        // In case of a single NP, we want to know what slot it is
        singleNpFieldSlot: FieldSlot? = null,
        chainPriority: List<ChainTypeEnum>? = null
    ): List<ParsedCard>? {
        val cardsToFind = totalUniqueCardTypesPermitted
        val uniqueCardTypes = uniqueCardTypesAlreadyFilled.toMutableSet()

        val newList = mutableListOf<ParsedCard>()
        var filteredCards = cards
        while (uniqueCardTypes.size < cardsToFind) {
            filteredCards = filteredCards.filter {
                // Always look for a different card type
                it.type !in uniqueCardTypes
            }
            // If there is a single NP, we want to try for a Brave Mighty Chain
            if (singleNpFieldSlot != null) {
                // Attempt to find one matching the fieldSlot
                val fieldSlotList = filteredCards.filter {
                    it.fieldSlot == singleNpFieldSlot
                }
                if (fieldSlotList.isNotEmpty()) filteredCards = fieldSlotList
            }
            val filteredCard = filteredCards.firstOrNull()
            if (filteredCard == null) break; // If cannot find, we leave
            uniqueCardTypes.add(filteredCard.type)
            newList.add(filteredCard)
        }

        // If there isn't a valid list of cards, we reject and have empty be returned
        if (uniqueCardTypes.size < cardsToFind) return null

        // Otherwise, we return the expected output
        val remainder = cards - newList
        val combinedCards = newList + remainder

        return combinedCards
    }

    fun isMightyChainAllowed (
        npUsage: NPUsage,
        uniqueCardTypesFromNp: Set<CardTypeEnum>,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap()
    ): Boolean {
        val npUsageSize = npUsage.nps.size

        // if there are more than 1 NP and we don't know their types,
        // just do the default since we can't guarantee a mighty chain anyway
        if (npUsageSize > 0 && npTypes.size != npUsageSize) return false

        // We stop if
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