package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.collections.plus

@ScriptScope
class CardChainPriorityHandler @Inject constructor(
    private val mightyChainHandler: MightyChainHandler,
) {
    fun pick(
        cards: List<ParsedCard>,
        chainPriority: List<ChainTypeEnum> = ChainTypeEnum.defaultOrder,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        cardCountPerFieldSlotMap: Map<FieldSlot, Int>? = null,
        cardCountPerCardTypeMap: Map<CardTypeEnum, Int>? = null,
        forceBraveChain: Boolean = false,
    ): List<ParsedCard>? {
        // Try to ensure unknown is handled
        val nonUnknownCards = AttackUtils.getValidNonUnknownCards(cards)
        if (!AttackUtils.isChainable(
            cards = nonUnknownCards,
            npUsage = npUsage,
            npTypes = npTypes,
        )) {
            return nonUnknownCards + (cards - nonUnknownCards)
        }

        var newCardOrder: List<ParsedCard>? = null
        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: AttackUtils.getCardsPerFieldSlotMap(nonUnknownCards, npUsage)
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: AttackUtils.getCardsPerCardTypeMap(nonUnknownCards, npTypes)

        for (chain in chainPriority) {
            if (newCardOrder != null) continue
            newCardOrder = when (chain) {
                ChainTypeEnum.Mighty -> mightyChainHandler.pick(
                    cards = nonUnknownCards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    braveChainEnum = braveChainEnum,
                    forceBraveChain = forceBraveChain,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                    cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
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
                    forceBraveChain = forceBraveChain,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                    cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                )
                ChainTypeEnum.Avoid -> AvoidChainHandler.pick(
                    cards = nonUnknownCards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    avoidBraveChains = braveChainEnum == BraveChainEnum.Avoid,
                    avoidCardChains = true,
                    cardCountPerFieldSlotMap = cardCountPerFieldSlotMap,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                )
            }
        }

        return newCardOrder
    }
}