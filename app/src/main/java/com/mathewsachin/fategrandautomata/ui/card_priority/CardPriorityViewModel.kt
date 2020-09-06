package com.mathewsachin.fategrandautomata.ui.card_priority

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.prefs.defaultCardPriority
import com.mathewsachin.fategrandautomata.scripts.enums.BraveChainEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriority
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave

class CardPriorityViewModel @ViewModelInject constructor(
    val prefsCore: PrefsCore,
    @Assisted savedState: SavedStateHandle
) : ViewModel() {
    val autoSkillItemKey: String = savedState[CardPriorityFragmentArgs::key.name]
        ?: throw kotlin.Exception("Couldn't get AutoSkill key")

    private val autoSkillPref = prefsCore.forAutoSkillConfig(autoSkillItemKey)

    val cardPriorityItems: MutableList<CardPriorityListItem> by lazy {
        var cardPriority = autoSkillPref.cardPriority.get()

        // Handle simple mode and empty string
        if (cardPriority.length == 3 || cardPriority.isBlank()) {
            cardPriority = defaultCardPriority
        }

        val rearrangeCards = autoSkillPref.rearrangeCards
        val braveChains = autoSkillPref.braveChains

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

    val experimental = autoSkillPref
        .experimental
        .asFlow()
        .asLiveData()

    fun setExperimental(value: Boolean) = autoSkillPref.experimental.set(value)

    fun save() {
        val value = CardPriorityPerWave.from(
            cardPriorityItems.map {
                CardPriority.from(it.scores)
            }
        ).toString()

        autoSkillPref.cardPriority.set(value)
        autoSkillPref.rearrangeCards = if (experimental.value == true)
            cardPriorityItems.map { it.rearrangeCards }
        else emptyList()

        autoSkillPref.braveChains = if (experimental.value == true)
            cardPriorityItems.map { it.braveChains }
        else emptyList()
    }
}