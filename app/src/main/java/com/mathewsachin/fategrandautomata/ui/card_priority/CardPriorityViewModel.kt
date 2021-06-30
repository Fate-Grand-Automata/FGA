package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave

class CardPriorityViewModel @ViewModelInject constructor(
    val prefsCore: PrefsCore,
    @Assisted savedState: SavedStateHandle
) : ViewModel() {
    val battleConfigKey: String = savedState[CardPriorityFragmentArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get Battle Config key")

    private val battleConfig = prefsCore.forBattleConfig(battleConfigKey)

    val cardPriorityItems: MutableList<CardPriorityListItem> by lazy {
        var cardPriority = battleConfig.cardPriority.get()

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank()) {
            cardPriority = defaultCardPriority
        }

        val rearrangeCards = battleConfig.rearrangeCards
        val braveChains = battleConfig.braveChains

        CardPriorityPerWave.of(cardPriority)
            .map { it.toMutableList() }
            .withIndex()
            .map {
                CardPriorityListItem(
                    it.value,
                    rearrangeCards.getOrElse(it.index) { false },
                    braveChains.getOrElse(it.index) { BraveChainEnum.None }
                )
            }
            .toMutableList()
    }

    fun save() {
        val value = CardPriorityPerWave.from(
            cardPriorityItems.map {
                CardPriority.from(it.scores)
            }
        ).toString()

        battleConfig.cardPriority.set(value)
        battleConfig.rearrangeCards = cardPriorityItems.map { it.rearrangeCards }
        battleConfig.braveChains = cardPriorityItems.map { it.braveChains }
    }
}