package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.Map

@ScriptScope
class MightyChainHandler @Inject constructor(
    private val utils: Utils
) {
    // We want 3 unique types, the magic number
    val totalUniqueCardTypesPermitted = 3

    // Returns null if uniqueCards cannot be found
    fun pick (
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
        forceBraveChain: Boolean = false,
    ): List<ParsedCard>? {
        val uniqueCardTypesFromNp = npTypes.values.toSet()
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: utils.getCardsPerCardTypeMap(cards, npTypes)
        if (!isMightyChainAllowed(
            cards = cards,
            npUsage = npUsage,
            uniqueCardTypesFromNp = uniqueCardTypesFromNp,
            npTypes = npTypes,
            cardCountPerCardTypeMap = cardCountPerCardTypeMap
        )) return null

        // Check for Brave Chain
        val braveChainFieldSlot = utils.getBraveChainFieldSlot(
            cards = cards,
            braveChainEnum = braveChainEnum,
            npUsage = npUsage,
            forceBraveChain = forceBraveChain,
        )

        return pick(
            cards = cards,
            uniqueCardTypesAlreadyFilled = uniqueCardTypesFromNp,
            braveChainFieldSlot = braveChainFieldSlot,
            braveChainEnum = braveChainEnum,
            forceBraveChain = forceBraveChain,
        )
    }

    // Returns null if uniqueCards cannot be found
    private fun pick(
        cards: List<ParsedCard>,
        // Decreases number of cards to get
        // e.g. usually based on npSize
        uniqueCardTypesAlreadyFilled: Set<CardTypeEnum>,
        // In case of a Brave chain, we want to know what slot it is
        braveChainFieldSlot: FieldSlot? = null,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        forceBraveChain: Boolean = false,
    ): List<ParsedCard>? {
        val cardsToFind = totalUniqueCardTypesPermitted
        val uniqueCardTypes = uniqueCardTypesAlreadyFilled.toMutableSet()

        val selectedCards = mutableListOf<ParsedCard>()
        while (uniqueCardTypes.size < cardsToFind) {
            var filteredCards = cards.filter {
                // Always look for a different card type
                it.type !in uniqueCardTypes
            }
            // If there is a single NP, we want to try for a Brave Mighty Chain
            if (braveChainFieldSlot != null && braveChainEnum != BraveChainEnum.Avoid) {
                // Attempt to find one matching the fieldSlot
                val fieldSlotList = filteredCards.filter {
                    it.fieldSlot == braveChainFieldSlot
                }
                // Even if it is empty, if forceBraveChain is on,
                // it only accepts Brave Mighty Chains and not normal Mighty Chains
                if (fieldSlotList.isNotEmpty() || forceBraveChain) filteredCards = fieldSlotList
            }
            val filteredCard = filteredCards.firstOrNull()
            if (filteredCard == null) break // If cannot find, we leave
            uniqueCardTypes.add(filteredCard.type)
            selectedCards.add(filteredCard)
        }

        // If there isn't a valid list of cards, we reject and have empty be returned
        if (uniqueCardTypes.size < cardsToFind) return null

        // Otherwise, we return the expected output
        val remainder = cards - selectedCards
        val combinedCards = selectedCards + remainder

        return combinedCards
    }

    fun isMightyChainAllowed (
        cards: List<ParsedCard>,
        npUsage: NPUsage,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        uniqueCardTypesFromNp: Set<CardTypeEnum>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
    ): Boolean {
        val uniqueCardTypesFromNp = uniqueCardTypesFromNp ?: npTypes.values.toSet()
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: utils.getCardsPerCardTypeMap(cards, npTypes)
        val npUsageSize = npUsage.nps.size

        // if there are more than 1 NP and we don't know their types,
        // just do the default since we can't guarantee a mighty chain anyway
        if (npUsageSize > 0 && npTypes.size != npUsageSize) return false

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