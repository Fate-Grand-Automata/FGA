package io.github.fate_grand_automata.scripts.interfaces

import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.FieldSlot
import io.github.fate_grand_automata.scripts.models.NPUsage
import io.github.fate_grand_automata.scripts.models.ParsedCard

interface AttackChainInterface {
    /**
     * Checks if the currently selected Chain is valid
     *
     * @param cards Cards available for this attack
     * @param npUsage NPs in use for this attack
     * @param npTypes Card Types of the NP in use for this attack
     * @param braveChainEnum Brave chain settings for this attack
     * @param hasServantPriority Is Servant Priority in use for this attack
     * @param forceBraveChain Allow only Brave Chains to succeed
     */
    fun isAttackChainAllowed(
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.Companion.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        hasServantPriority: Boolean = false,
        forceBraveChain: Boolean = false,
    ): Boolean

    /**
     * Runs the selection of the cards using this Chain.
     * Expects to return null if Chain cannot be performed.
     *
     * @param cards Cards available for this attack
     * @param npUsage NPs in use for this attack
     * @param npTypes Card Types of the NP in use for this attack
     * @param braveChainEnum Brave chain settings for this attack
     * @param hasServantPriority Is Servant Priority in use for this attack
     * @param forceBraveChain Allow only Brave Chains to succeed
     */
    fun pick(
        cards: List<ParsedCard>,
        npUsage: NPUsage = NPUsage.Companion.none,
        npTypes: Map<FieldSlot, CardTypeEnum> = emptyMap(),
        braveChainEnum: BraveChainEnum = BraveChainEnum.None,
        hasServantPriority: Boolean = false,
        forceBraveChain: Boolean = false,
    ): List<ParsedCard>?
}