package io.github.fate_grand_automata.ui.fine_tune

import androidx.annotation.StringRes
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.ui.VectorIcon
import kotlin.math.roundToInt

class FineTuneItem(
    val pref: Pref<Int>,
    @param:StringRes val name: Int,
    val icon: VectorIcon,
    val valueRange: IntRange = 0..100,
    val step: Int = 1,
    val valueRepresentation: (Int) -> String = { it.toString() },
    @param:StringRes val hint: Int
) {
    fun reset() {
        pref.resetToDefault()
    }

    /**
     * Rounds [raw] to the nearest multiple of [step], clamped into [valueRange].
     *
     * Anchored on zero rather than on the range start, so a range that doesn't begin on a multiple
     * still yields round numbers.
     */
    fun snapToStep(raw: Float) =
        ((raw / step).roundToInt() * step)
            .coerceIn(valueRange.first, valueRange.last)

    /**
     * Moves an explicitly stored value onto a position the slider can actually show.
     *
     * [valueRange] and [step] change between app versions, so a stored value can fall outside the
     * range or between two slider positions. Without this the slider would display the nearest
     * reachable value while the scripts kept using the stored one.
     */
    fun normalizeStoredValue() {
        if (pref.isSet()) {
            pref.set(snapToStep(pref.get().toFloat()))
        }
    }
}

class FineTuneGroup(
    @param:StringRes val name: Int,
    val items: List<FineTuneItem>
)