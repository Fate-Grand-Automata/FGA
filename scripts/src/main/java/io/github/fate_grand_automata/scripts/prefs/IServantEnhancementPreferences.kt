package io.github.fate_grand_automata.scripts.prefs

interface IServantEnhancementPreferences {

    var shouldLimit: Boolean
    var limitCount: Int
    var shouldRedirectAscension: Boolean
    val shouldPerformAscension: Boolean

    var shouldRedirectGrail: Boolean

    var muteNotifications: Boolean
}