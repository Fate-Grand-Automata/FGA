package io.github.fate_grand_automata.scripts.prefs

interface ICraftEssencePreferences {

    var emptyEnhance: Boolean

    var skipCEFilterDetection: Boolean

    val ceFodderRarity: List<Int>

    var ceTargetRarity: Int

    var skipAutomaticDisplayChange: Boolean

    var topRightDisplayLocation: Boolean
}