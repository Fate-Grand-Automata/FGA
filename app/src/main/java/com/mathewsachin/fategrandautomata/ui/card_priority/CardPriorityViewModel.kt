package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardPriorityViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val battleConfig: BattleConfigCore
) : ViewModel() {
    val cardPriorityItems: SnapshotStateList<CardPriorityListItem> by lazy {
        var cardPriority = battleConfig.cardPriority.get()

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank()) {
            cardPriority = defaultCardPriority
        }

        val rearrangeCards = battleConfig.rearrangeCards
        val braveChains = battleConfig.braveChains

        CardPriorityPerWave.of(cardPriority)
            .take(3)
            .map { it.toMutableList() }
            .withIndex()
            .map {
                CardPriorityListItem(
                    it.value,
                    mutableStateOf(rearrangeCards.get().getOrElse(it.index) { false }),
                    mutableStateOf(braveChains.get().getOrElse(it.index) { BraveChainEnum.None })
                )
            }
            .toMutableStateList()
    }

    fun save() {
        val value = CardPriorityPerWave.from(
            cardPriorityItems.map {
                CardPriority.from(it.scores)
            }
        ).toString()

        battleConfig.cardPriority.set(value)
        battleConfig.rearrangeCards.set(cardPriorityItems.map { it.rearrangeCards.value })
        battleConfig.braveChains.set(cardPriorityItems.map { it.braveChains.value })
    }
}