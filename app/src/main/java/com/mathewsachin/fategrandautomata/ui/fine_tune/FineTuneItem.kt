package com.mathewsachin.fategrandautomata.ui.fine_tune

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import com.mathewsachin.fategrandautomata.prefs.core.Pref

class FineTuneItem(
    val pref: Pref<Int>,
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val valueRange: IntRange = 0..100,
    val valueRepresentation: (Int) -> String = { it.toString() },
    // TODO: Localize fine-tune hints
    val hint: String = ""
) {
    val state = mutableStateOf(pref.get().toFloat())

    fun reset() {
        pref.resetToDefault()
        state.value = pref.defaultValue.toFloat()
    }
}

class FineTuneGroup(
    @StringRes val name: Int,
    val items: List<FineTuneItem>
)