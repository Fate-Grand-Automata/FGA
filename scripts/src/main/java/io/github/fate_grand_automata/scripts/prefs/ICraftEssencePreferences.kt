package io.github.fate_grand_automata.scripts.prefs

import io.github.fate_grand_automata.scripts.enums.CEDisplayChangeAreaEnum

interface ICraftEssencePreferences {

    var emptyEnhance: Boolean

    var skipSortDetection: Boolean

    var skipCEFilterDetection: Boolean

    val ceFodderRarity: List<Int>

    var ceTargetRarity: Int

    var skipAutomaticDisplayChange: Boolean

    var canShowAutomaticDisplayChange: Boolean

    var ceDisplayChangeArea: Set<CEDisplayChangeAreaEnum>

    fun updateCeDisplayChangeArea(area: CEDisplayChangeAreaEnum)

    var useDragging: Boolean
}