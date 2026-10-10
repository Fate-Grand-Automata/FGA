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
import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.AutoSkillCommand
import io.github.fate_grand_automata.scripts.models.CardPriority
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.scripts.models.ServantPriorityPerWave

data class MightyChainAttack(
    val npSlots: List<Int>,
    val cardsBeforeNp: Int,
)

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

        val rearrangeCards = battleConfig.rearrangeCards.get()
        val braveChains = battleConfig.braveChains.get()

        cardPriority
            .take(3)
            .map { it.toMutableStateList() }
            .withIndex()
            .map {
                CardPriorityListItem(
                    it.value,
                    servantPriority.atWave(it.index).toMutableStateList(),
                    mutableStateOf(rearrangeCards.getOrElse(it.index) { false }),
                    mutableStateOf(braveChains.getOrElse(it.index) { BraveChainEnum.None })
                )
            }
            .toMutableStateList()
    }

    val useServantPriority = battleConfig.useServantPriority
    val useMightyChain = battleConfig.useMightyChain
    val npCardTypes = battleConfig.npCardTypes
    val mightyChainOrders = battleConfig.mightyChainOrders.get()
        .map { it.toMutableStateList() }
    val mightyChainTurns: List<List<MightyChainAttack>> = runCatching {
        AutoSkillCommand.parse(battleConfig.skillCommand.get()).stages.map { turns ->
            turns.mapNotNull { actions ->
                actions.filterIsInstance<AutoSkillAction.Atk>()
                    .firstOrNull { it.nps.isNotEmpty() }
                    ?.let { attack ->
                        MightyChainAttack(
                            npSlots = attack.nps.mapNotNull {
                                CommandCard.NP.list.indexOf(it).takeIf { index -> index >= 0 }
                            },
                            cardsBeforeNp = attack.cardsBeforeNP,
                        )
                    }
            }
        }
    }.getOrDefault(emptyList())

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

        battleConfig.rearrangeCards.set(cardPriorityItems.map { it.rearrangeCards.value })
        battleConfig.braveChains.set(cardPriorityItems.map { it.braveChains.value })
        battleConfig.mightyChainOrders.set(mightyChainOrders.map { it.toList() })
    }
}
