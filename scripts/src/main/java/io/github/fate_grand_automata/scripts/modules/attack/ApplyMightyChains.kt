package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.interfaces.AttackChainInterface
import io.github.fate_grand_automata.scripts.models.toFieldSlot
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ApplyMightyChains @Inject constructor(
    private val utils: Utils
): AttackChainInterface {
    // We want 3 unique types, the magic number
    val totalUniqueCardTypesPermitted = 3

    // Returns null if uniqueCards cannot be found
    override fun pick (
        cards: List<ParsedCard>,
        npUsage: NPUsage,
        npTypes: Map<FieldSlot, CardTypeEnum>,
        braveChainEnum: BraveChainEnum,
        forceServantPriority: Boolean,
        forceBraveChain: Boolean,
    ): List<ParsedCard>? {
        val uniqueCardTypesFromNp = npTypes.values.toSet()
        if (!isMightyChainAllowed(npUsage, uniqueCardTypesFromNp, npTypes)) return null

        // Check for Brave Chain
        val braveChainFieldSlot =
            if (braveChainEnum == BraveChainEnum.Avoid) null
            else if (npUsage.nps.size == 1) {
                // Get np if there is only 1 (since we want to try for Brave Chain)
                val firstNp = npUsage.nps.firstOrNull()
                firstNp?.toFieldSlot()
            } else {
                val braveChainCapableFieldSlots = utils.getFieldSlotsWithValidBraveChain(cards, npUsage)
                // If there is Servant Priority, the first card is most important.
                // Meaning if first card cannot Brave Chain, we ignore Brave Chain
                if (forceServantPriority) {
                    val firstCardFieldSlot = cards.firstOrNull()?.fieldSlot
                    if (braveChainCapableFieldSlots.contains(firstCardFieldSlot)) firstCardFieldSlot
                    else null
                }
                val braveChainPriorityCard = cards.firstOrNull { braveChainCapableFieldSlots.contains(it.fieldSlot) }
                // Return the first valid one, since it is already the highest priority
                braveChainPriorityCard?.fieldSlot
            }

        return pick(
            cards = cards,
            uniqueCardTypesAlreadyFilled = uniqueCardTypesFromNp,
            fieldSlotForBraveChain = braveChainFieldSlot,
            braveChainEnum = braveChainEnum,
            forceServantPriority = forceServantPriority,
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
        fieldSlotForBraveChain: FieldSlot? = null,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        forceServantPriority: Boolean = false,
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
            if (fieldSlotForBraveChain != null) {
                // Attempt to find one matching the fieldSlot
                val fieldSlotList = filteredCards.filter {
                    it.fieldSlot == fieldSlotForBraveChain
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

    override fun isAttackChainAllowed (
        cards: List<ParsedCard>,
        npUsage: NPUsage,
        npTypes: Map<FieldSlot, CardTypeEnum>,
        braveChainEnum: BraveChainEnum,
        forceServantPriority: Boolean,
        forceBraveChain: Boolean,
    ): Boolean {
        val uniqueCardTypesFromNp = npTypes.values.toSet()
        return isMightyChainAllowed(npUsage, uniqueCardTypesFromNp, npTypes)
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