package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.CraftEssencePrefsCore
import io.github.fate_grand_automata.scripts.prefs.ICraftEssencePreferences

internal class CraftEssencePrefs(
    val prefs: CraftEssencePrefsCore
) : ICraftEssencePreferences {
    override var emptyEnhance: Boolean by prefs.emptyEnhance
}