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
    private val applyMightyChains: ApplyMightyChains,
) {
    fun pick(
        cards: List<ParsedCard>,
        chainPriority: List<ChainTypeEnum> = ChainTypeEnum.defaultOrder,
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        npUsage: NPUsage = NPUsage.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        hasServantPriority: Boolean = false,
        forceBraveChain: Boolean = false,
    ): List<ParsedCard>? {
        var newCardOrder: List<ParsedCard>? = null
        for (chain in chainPriority) {
            if (newCardOrder != null) continue
            newCardOrder = when (chain) {
                ChainTypeEnum.Mighty -> applyMightyChains.pick(
                    cards = cards,
                    npUsage = npUsage,
                    npTypes = npTypes,
                    forceBraveChain = forceBraveChain
                )
                else -> null
            }
        }

        return newCardOrder
    }
}