package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.prefs.core.RefillPrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IRefillPreferences

internal class RefillPreferences(val prefs: RefillPrefsCore) :
    IRefillPreferences {
    override val enabled by prefs.enabled

    override var repetitions by prefs.repetitions

    override val resource by prefs.resource

    override val autoDecrement by prefs.autoDecrement

    override val shouldLimitRuns by prefs.shouldLimitRuns

    override val limitRuns by prefs.limitRuns
}