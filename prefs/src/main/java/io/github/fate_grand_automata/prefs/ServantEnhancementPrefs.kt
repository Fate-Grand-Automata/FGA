package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.ServantEnhancementPrefsCore
import io.github.fate_grand_automata.scripts.prefs.IServantEnhancementPreferences

internal class ServantEnhancementPrefs(
    val prefsCore: ServantEnhancementPrefsCore
) : IServantEnhancementPreferences {
    override var shouldRedirectAscension: Boolean by prefsCore.shouldRedirectAscension
    override var shouldPerformAscension: Boolean by prefsCore.shouldPerformAscension
    override var shouldRedirectGrail: Boolean by prefsCore.shouldRedirectGrail
}