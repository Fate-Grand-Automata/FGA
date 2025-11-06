package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.lib_automata.Hsv
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CriticalStarTracker @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {

    companion object {

        // OCR Accuracy Report
        //       0 1 2 3 4 5 6 7 8 9
        // TW:   ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅
        // CN:   ✅ ⚠️ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅
        // KO:   ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅
        // JA:   ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅
        // EN:   ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅ ✅
        // ⚠️: Sometimes a “1” is misrecognized as a “4”.
        //     Safe with current Star Condition options.
        private val whiteTextHsvLower = Hsv(0.0, 0.0, 55.0)
        private val whiteTextHsvUpper = Hsv(150.0, 8.0, 255.0)
    }

    var starCount = 0
        private set

    /**
     * Updates the current star count using a color snapshot.
     *
     * ⚠️ When calling this function inside `useSameSnapIn`, be aware that it may
     * reuse a gray cached snapshot and return incorrect results.
     */
    fun updateStarCount() {
        starCount = locations.battle.criticalStarRegion.detectNumberInBrackets(
            lower = whiteTextHsvLower,
            upper = whiteTextHsvUpper,
            invert = true
        ).let {
            parseStarCount(it).coerceIn(0, 99)
        }
    }

    private fun parseStarCount(starText: String): Int {
        if (starText.isEmpty()) return 0
        return starText.toIntOrNull() ?: 0
    }
}