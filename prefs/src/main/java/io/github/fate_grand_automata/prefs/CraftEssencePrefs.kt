package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.CraftEssencePrefsCore
import io.github.fate_grand_automata.prefs.core.map
import io.github.fate_grand_automata.scripts.enums.CEDisplayChangeAreaEnum
import io.github.fate_grand_automata.scripts.prefs.ICraftEssencePreferences

internal class CraftEssencePrefs(
    val prefs: CraftEssencePrefsCore
) : ICraftEssencePreferences {
    override var emptyEnhance: Boolean by prefs.emptyEnhance

    override var ceTargetRarity: Int by prefs.ceTargetRarity

    override var skipSortDetection: Boolean by prefs.skipSortDetection

    override var skipCEFilterDetection: Boolean by prefs.skipCEFilterDetection

    override val ceFodderRarity: List<Int> by prefs.ceFodderRarity.map {
        it.sorted()
    }

    override var skipAutomaticDisplayChange: Boolean by prefs.skipAutomaticDisplayChange

    override var canShowAutomaticDisplayChange: Boolean by prefs.canShowAutomaticDisplayChange

    override var ceDisplayChangeArea: Set<CEDisplayChangeAreaEnum> by prefs.ceDisplayChangeArea

    override fun updateCeDisplayChangeArea(area: CEDisplayChangeAreaEnum) =
        prefs.ceDisplayChangeArea.set(setOf(area))

    override var useDragging: Boolean by prefs.useDragging

    override var skipAutoLockTargetCE: Boolean by prefs.skipAutoLockTargetCE
}