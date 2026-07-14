package io.github.fate_grand_automata.ui.custom_card_selection

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.models.CustomCardSelection
import io.github.fate_grand_automata.scripts.models.CustomCardSelectionPerTurn
import javax.inject.Inject

@HiltViewModel
class CustomCardSelectionViewModel @Inject constructor(
    val battleConfig: BattleConfigCore
) : ViewModel() {
    val turnSelections: SnapshotStateList<CustomCardSelection> by lazy {
        battleConfig.customCardSelection.get().toMutableStateList()
    }

    fun addTurn() {
        turnSelections.add(CustomCardSelection.empty)
        save()
    }

    fun removeTurn(index: Int) {
        if (index in turnSelections.indices) {
            turnSelections.removeAt(index)
            save()
        }
    }

    fun updateTurn(index: Int, selection: CustomCardSelection) {
        if (index in turnSelections.indices) {
            turnSelections[index] = selection
            save()
        }
    }

    fun save() {
        val perTurn = CustomCardSelectionPerTurn.from(turnSelections.toList())
        battleConfig.customCardSelection.set(perTurn)
    }
}
