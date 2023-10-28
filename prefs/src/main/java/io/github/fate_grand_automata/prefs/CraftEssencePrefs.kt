package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.CraftEssencePrefsCore
import io.github.fate_grand_automata.prefs.core.map
import io.github.fate_grand_automata.scripts.prefs.ICraftEssencePreferences

internal class CraftEssencePrefs(
    val prefs: CraftEssencePrefsCore
) : ICraftEssencePreferences {
    override var emptyEnhance: Boolean by prefs.emptyEnhance

    override var ceTargetRarity: Int by prefs.ceTargetRarity

    override var skipCEFilterDetection: Boolean by prefs.skipCEFilterDetection

    override val ceFodderRarity: List<Int> by prefs.ceFodderRarity.map{
        it.sorted()
    }

    override var skipAutomaticDisplayChange: Boolean by prefs.skipAutomaticDisplayChange
}