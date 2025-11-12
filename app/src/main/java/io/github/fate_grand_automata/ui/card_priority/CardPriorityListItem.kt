package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.runtime.MutableState
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.enums.ChainTypeEnum
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.scripts.models.TeamSlot

data class CardPriorityListItem(
    val scores: MutableList<CardScore>,
    val servantPriority: MutableList<TeamSlot>,
    val chainPriority: MutableList<ChainTypeEnum>,
    var rearrangeCards: MutableState<Boolean>,
    var braveChains: MutableState<BraveChainEnum>
)