package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.runtime.MutableState
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.models.TeamSlot

data class CardPriorityListItem(
    val scores: MutableList<CardScore>,
    val servantPriority: MutableList<TeamSlot>,
    var rearrangeCards: MutableState<Boolean>,
    var braveChains: MutableState<BraveChainEnum>
)