package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.enums.BraveChainEnum
import io.github.fate_grand_automata.scripts.models.CardPriority
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave
import io.github.fate_grand_automata.scripts.models.battle.ChainPriorityPerWave

@HiltViewModel(assistedFactory = CardPriorityViewModel.Factory::class)
class CardPriorityViewModel @AssistedInject constructor(
    prefsCore: PrefsCore,
    @Assisted id: String
) : ViewModel() {
    val battleConfig = prefsCore.forBattleConfig(id)

    @AssistedFactory
    interface Factory {
        fun create(id: String): CardPriorityViewModel
    }
    val cardPriorityItems: SnapshotStateList<CardPriorityListItem> by lazy {
        val cardPriority = battleConfig.cardPriority.get()
        val servantPriority = battleConfig.servantPriority.get()
        val chainPriority = battleConfig.chainPriority.get()

        val rearrangeCards = battleConfig.rearrangeCards.get()
        val braveChains = battleConfig.braveChains.get()
        val useServantPriority = battleConfig.useServantPriority.get()
        val useChainPriority = battleConfig.useChainPriority.get()

        cardPriority
            .take(3)
            .map { it.toMutableStateList() }
            .withIndex()
            .map {
                CardPriorityListItem(
                    it.value,
                    servantPriority.atWave(it.index).toMutableStateList(),
                    chainPriority.atWave(it.index).toMutableStateList(),
                    mutableStateOf(rearrangeCards.getOrElse(it.index) { false }),
                    mutableStateOf(braveChains.getOrElse(it.index) { BraveChainEnum.None }),
                    // Waves past the end use the last value, as in a battle
                    mutableStateOf(useServantPriority[it.index.coerceAtMost(useServantPriority.lastIndex)]),
                    mutableStateOf(useChainPriority[it.index.coerceAtMost(useChainPriority.lastIndex)])
                )
            }
            .toMutableStateList()
    }

    fun save() {
        battleConfig.cardPriority.set(
            CardPriorityPerWave.from(
                cardPriorityItems.map { CardPriority.from(it.scores.toList()) }
            )
        )

        battleConfig.servantPriority.set(
            ServantPriorityPerWave.from(
                cardPriorityItems.map { it.servantPriority.toList() }
            )
        )

        battleConfig.chainPriority.set(
            ChainPriorityPerWave.from(
                cardPriorityItems.map { it.chainPriority.toList() }
            )
        )

        battleConfig.rearrangeCards.set(cardPriorityItems.map { it.rearrangeCards.value })
        battleConfig.braveChains.set(cardPriorityItems.map { it.braveChains.value })
        battleConfig.useServantPriority.set(cardPriorityItems.map { it.useServantPriority.value })
        battleConfig.useChainPriority.set(cardPriorityItems.map { it.useChainPriority.value })
    }
}