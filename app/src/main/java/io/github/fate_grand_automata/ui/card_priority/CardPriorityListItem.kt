package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.scripts.models.TeamSlot
import io.github.fate_grand_automata.scripts.models.battle.ChainPriorityItem

data class CardPriorityListItem(
    val scores: SnapshotStateList<CardScore>,
    val servantPriority: SnapshotStateList<TeamSlot>,
    val chainPriority: SnapshotStateList<ChainPriorityItem>,
    var rearrangeCards: MutableState<Boolean>,
    var braveChains: MutableState<BraveChainEnum>,
    val useServantPriority: MutableState<Boolean>,
    val useChainPriority: MutableState<Boolean>
)
