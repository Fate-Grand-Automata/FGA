package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.fate_grand_automata.scripts.models.toFieldSlot

object AvoidChainHandler {
    fun pick(
        cards: List<ParsedCard>,
        braveChainEnum: BraveChainEnum = BraveChainEnum.Avoid,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
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

        // Handle brave chains
        // If there is a valid braveChainFieldSlot, it means that a Brave Chain must be made while avoiding
        // Color + Mighty Chains while avoidCardChains = true
        val avoidBraveChains = braveChainEnum == BraveChainEnum.Avoid
        val braveChainFieldSlot = AttackUtils.getBraveChainFieldSlot(
            braveChainEnum = braveChainEnum,
            cards = nonUnknownCards,
            npUsage = npUsage,
        )

        // This is just a normal BraveChain requirement. Go to BraveChainHandler for this
        if (braveChainFieldSlot != null && !avoidCardChains) return null

        // Filter BraveChain-able only cards or skip filter (if there is no BraveChain required)
        val cardsToUse = nonUnknownCards.filter { braveChainFieldSlot == null || it.fieldSlot == braveChainFieldSlot }

        // The search rejects face cards with unknown slots when avoiding Brave Chains.
        // If all usable face cards and every NP share a slot, any complete pick is a Brave Chain.
        val singleFaceCardSlot = cardsToUse.firstNotNullOfOrNull { it.fieldSlot }
        val onlyBraveChainPossible = avoidBraveChains && singleFaceCardSlot != null &&
            cardsToUse.all { it.fieldSlot == null || it.fieldSlot == singleFaceCardSlot } &&
            npUsage.nps.all { it.toFieldSlot() == singleFaceCardSlot }
        if (onlyBraveChainPossible) return null

        // A Color Chain is forced when every face card and every scheduled NP has the same type.
        // Each NP must have a matching type because the search counts only supplied NP types.
        val singleFaceCardType = cardsToUse.firstOrNull()?.type
        val onlyColorChainPossible = avoidCardChains && singleFaceCardType != null &&
            cardsToUse.all { it.type == singleFaceCardType } &&
            npUsage.nps.all { npTypes[it.toFieldSlot()] == singleFaceCardType }
        if (onlyColorChainPossible) return null

        // Let selectCards evaluate the remaining combinations using the NP counts as its starting state.
        val npFieldSlotCounts =
            if (avoidBraveChains) npUsage.nps.groupingBy { it.toFieldSlot() }.eachCount()
            else emptyMap()
        val npCardTypeCounts =
            if (avoidCardChains) npTypes.values.groupingBy { it }.eachCount()
            else emptyMap()

        val selectedCards = selectCards(
            candidates = cardsToUse,
            cardsNeeded = 3 - npUsage.nps.size,
            selected = emptyList(),
            fieldSlotCounts = npFieldSlotCounts,
            cardTypeCounts = npCardTypeCounts,
            avoidBraveChains = avoidBraveChains,
            avoidCardChains = avoidCardChains,
            // A required Brave Chain must not be broken up
            preferDifferentServant = braveChainFieldSlot == null,
        ) ?: return null

        val remainder = (cardsToUse - selectedCards) + (cards - cardsToUse)
        val combinedCards = selectedCards + remainder

        return combinedCards
    }

    /**
     * Depth-first search for [cardsNeeded] cards that avoid Brave and/or card chains.
     * Candidates are tried in priority order, preferring a different Servant than the previous card,
     * so the first complete selection found is the highest-priority valid one.
     *
     * @param selected Cards picked so far, in click order.
     * @param fieldSlotCounts Cards per Servant picked so far, including NPs when avoiding Brave Chains.
     * @param cardTypeCounts Cards per type picked so far, including NPs when avoiding card chains.
     * @return The selected cards, or null if no valid selection exists.
     */
    private fun selectCards(
        candidates: List<ParsedCard>,
        cardsNeeded: Int,
        selected: List<ParsedCard>,
        fieldSlotCounts: Map<FieldSlot, Int>,
        cardTypeCounts: Map<CardTypeEnum, Int>,
        avoidBraveChains: Boolean,
        avoidCardChains: Boolean,
        preferDifferentServant: Boolean,
    ): List<ParsedCard>? {
        if (selected.size >= cardsNeeded) return selected

        val allowedCards = candidates.filter { card ->
            // A card whose Servant isn't known can only be used when Brave Chains don't matter
            val fieldSlot = card.fieldSlot
            if (fieldSlot == null && avoidBraveChains) return@filter false
            val wouldMakeBraveChain = fieldSlot != null && fieldSlotCounts.getOrElse(fieldSlot) { 0 } >= 2

            val cardTypeCount = cardTypeCounts.getOrElse(card.type) { 0 }
            val wouldMakeColorChain = cardTypeCount >= 2
            val wouldMakeMightyChain = cardTypeCount == 0 && cardTypeCounts.size >= 2

            card !in selected
                && !(avoidBraveChains && wouldMakeBraveChain)
                && !(avoidCardChains && (wouldMakeColorChain || wouldMakeMightyChain))
        }

        val previousFieldSlot = selected.lastOrNull()?.fieldSlot
        // sortedBy is stable, so priority order is kept within each group
        val orderedCards =
            if (preferDifferentServant && previousFieldSlot != null) allowedCards.sortedBy { it.fieldSlot == previousFieldSlot }
            else allowedCards

        for (card in orderedCards) {
            val fieldSlot = card.fieldSlot
            val result = selectCards(
                candidates = candidates,
                cardsNeeded = cardsNeeded,
                selected = selected + card,
                fieldSlotCounts =
                    if (fieldSlot == null) fieldSlotCounts
                    else fieldSlotCounts + (fieldSlot to fieldSlotCounts.getOrElse(fieldSlot) { 0 } + 1),
                cardTypeCounts = cardTypeCounts + (card.type to cardTypeCounts.getOrElse(card.type) { 0 } + 1),
                avoidBraveChains = avoidBraveChains,
                avoidCardChains = avoidCardChains,
                preferDifferentServant = preferDifferentServant,
            )
            if (result != null) return result
        }

        return null
    }
}
