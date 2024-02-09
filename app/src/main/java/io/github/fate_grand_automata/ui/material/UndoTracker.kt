package io.github.fate_grand_automata.ui.material

import androidx.compose.runtime.Immutable
import io.github.fate_grand_automata.scripts.enums.MaterialEnum

@Immutable
data class UndoTracker(
    val materialSet: Set<MaterialEnum>,
    val undoAction: UndoAction
)

enum class UndoAction {
    ADD,
    REMOVE,
    REMOVE_ALL
}
