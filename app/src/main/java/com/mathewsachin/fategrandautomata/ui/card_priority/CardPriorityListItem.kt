package com.mathewsachin.fategrandautomata.ui.card_priority

import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardScore

data class CardPriorityListItem(
    val scores: MutableList<CardScore>,
    var rearrangeCards: Boolean,
    var braveChains: BraveChainEnum
)