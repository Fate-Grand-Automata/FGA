package io.github.fate_grand_automata.ui.fine_tune

import androidx.annotation.StringRes
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.VectorIcon

class FineTuneItem(
    val pref: Pref<Int>,
    @param:StringRes val name: Int,
    val icon: VectorIcon,
    val valueRange: IntRange = 0..100,
    val valueRepresentation: (Int) -> String = { it.toString() },
    // TODO: Localize fine-tune hints
    val hint: String = ""
) {
    fun reset() {
        pref.resetToDefault()
    }
}

class FineTuneGroup(
    @param:StringRes val name: Int,
    val items: List<FineTuneItem>
)