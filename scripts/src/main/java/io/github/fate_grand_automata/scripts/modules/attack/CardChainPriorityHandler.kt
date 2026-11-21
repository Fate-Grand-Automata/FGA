package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import kotlin.collections.plus

internal object CardChainPriorityHandler {
    data class Plan(val cards: List<ParsedCard>, val chainType: ChainTypeEnum?)

    fun pick(
        cards: List<ParsedCard>,
        chainPriority: List<ChainTypeEnum> = ChainTypeEnum.defaultOrder,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
        forceBraveChain: Boolean = false,
    ): Plan? {
        // Try to ensure unknown is handled
        val nonUnknownCards = AttackUtils.getValidNonUnknownCards(cards)
        if (!AttackUtils.isChainable(
            cards = nonUnknownCards,
            npUsage = npUsage,
            npTypes = npTypes,
        )) {
            return Plan(nonUnknownCards + (cards - nonUnknownCards), chainType = null)
        }

        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: AttackUtils.getCardsPerFieldSlotMap(nonUnknownCards, npUsage)
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(nonUnknownCards, npTypes)

        // If forceBraveChain, set the braveChainEnum directly here
        val braveChainEnum = if (forceBraveChain) BraveChainEnum.Always else braveChainEnum

        for (chain in chainPriority) {
            val order = when (chain) {
                ChainTypeEnum.Mighty -> MightyChainHandler.pick(
                    cards = nonUnknownCards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    braveChainEnum = braveChainEnum,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                    cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                    forceBraveChain = forceBraveChain,
                )
                ChainTypeEnum.Arts,
                ChainTypeEnum.Buster,
                ChainTypeEnum.Quick -> ColorChainHandler.pick(
                    cardType = when (chain) {
                        ChainTypeEnum.Buster -> CardTypeEnum.Buster
                        ChainTypeEnum.Arts -> CardTypeEnum.Arts
                        ChainTypeEnum.Quick -> CardTypeEnum.Quick
                        else -> CardTypeEnum.Unknown
                    },
                    cards = nonUnknownCards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    braveChainEnum = braveChainEnum,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                    cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                    forceBraveChain = forceBraveChain,
                )
                ChainTypeEnum.Avoid -> AvoidChainHandler.pick(
                    cards = nonUnknownCards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    braveChainEnum = braveChainEnum,
                    avoidCardChains = true,
                )
            }
            if (order != null) return Plan(order, chain)
        }

        return null
    }
}
