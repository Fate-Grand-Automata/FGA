package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.ServantEnhancementPrefsCore
import io.github.fate_grand_automata.scripts.prefs.IServantEnhancementPreferences

internal class ServantEnhancementPrefs(
    val prefsCore: ServantEnhancementPrefsCore
) : IServantEnhancementPreferences {
    override var shouldLimit: Boolean by prefsCore.shouldLimit
    override var limitCount: Int by prefsCore.limitCount

    override var shouldRedirectAscension: Boolean by prefsCore.shouldRedirectAscension
    override var shouldRedirectGrail: Boolean by prefsCore.shouldRedirectGrail
}