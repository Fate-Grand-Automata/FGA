package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CardPriority
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import javax.inject.Inject

@HiltViewModel
class CardPriorityViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val battleConfig: BattleConfigCore
) : ViewModel() {
    val cardPriorityItems: SnapshotStateList<CardPriorityListItem> by lazy {
        val cardPriority = battleConfig.cardPriority.get()
        val servantPriority = battleConfig.servantPriority.get()

        val rearrangeCards = battleConfig.rearrangeCards.get()
        val braveChains = battleConfig.braveChains.get()

        cardPriority
            .take(3)
            .map { it.toMutableList() }
            .withIndex()
            .map {
                CardPriorityListItem(
                    it.value,
                    servantPriority.atWave(it.index).toMutableList(),
                    mutableStateOf(rearrangeCards.getOrElse(it.index) { false }),
                    mutableStateOf(braveChains.getOrElse(it.index) { BraveChainEnum.None })
                )
            }
            .toMutableStateList()
    }

    val useServantPriority = battleConfig.useServantPriority

    val readCriticalStarPriority = battleConfig.readCriticalStarPriority

    fun save() {
        battleConfig.cardPriority.set(
            CardPriorityPerWave.from(
                cardPriorityItems.map { CardPriority.from(it.scores) }
            )
        )

        battleConfig.servantPriority.set(
            ServantPriorityPerWave.from(
                cardPriorityItems.map { it.servantPriority }
            )
        )

        battleConfig.rearrangeCards.set(cardPriorityItems.map { it.rearrangeCards.value })
        battleConfig.braveChains.set(cardPriorityItems.map { it.braveChains.value })
    }
}