package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CardChainPriorityHandler @Inject constructor(
    private val mightyChainHandler: MightyChainHandler,
    private val colorChainHandler: ColorChainHandler,
    private val avoidChainHandler: AvoidChainHandler,
    private val utils: AttackUtils
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
        var newCardOrder: List<ParsedCard>? = null
        val cardCountPerFieldSlotMap = cardCountPerFieldSlotMap ?: utils.getCardsPerFieldSlotMap(cards, npUsage)
        val cardCountPerCardTypeMap = cardCountPerCardTypeMap ?: utils.getCardsPerCardTypeMap(cards, npTypes)

        for (chain in chainPriority) {
            if (newCardOrder != null) continue
            newCardOrder = when (chain) {
                ChainTypeEnum.Mighty -> mightyChainHandler.pick(
                    cards = cards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    braveChainEnum = braveChainEnum,
                    forceBraveChain = forceBraveChain,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                )
                ChainTypeEnum.Arts,
                ChainTypeEnum.Buster,
                ChainTypeEnum.Quick -> colorChainHandler.pick(
                    cardType = when (chain) {
                        ChainTypeEnum.Buster -> CardTypeEnum.Buster
                        ChainTypeEnum.Arts -> CardTypeEnum.Arts
                        ChainTypeEnum.Quick -> CardTypeEnum.Quick
                        else -> CardTypeEnum.Unknown
                    },
                    cards = cards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    braveChainEnum = braveChainEnum,
                    forceBraveChain = forceBraveChain,
                    cardCountPerCardTypeMap = cardCountPerCardTypeMap,
                )
                ChainTypeEnum.Avoid -> avoidChainHandler.pick(
                    cards = cards,
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