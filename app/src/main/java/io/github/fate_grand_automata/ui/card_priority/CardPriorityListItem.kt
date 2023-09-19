package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.runtime.MutableState
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.scripts.models.TeamSlot

data class CardPriorityListItem(
    var scores: MutableList<CardScore>,
    var servantPriority: MutableList<TeamSlot>,
    var rearrangeCards: MutableState<Boolean>,
    var braveChains: MutableState<BraveChainEnum>
)