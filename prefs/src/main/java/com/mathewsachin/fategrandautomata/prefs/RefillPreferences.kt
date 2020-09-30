package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.prefs.core.RefillPrefsCore
import com.mathewsachin.fategrandautomata.prefs.core.map
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IRefillPreferences

internal class RefillPreferences(val prefs: RefillPrefsCore) :
    IRefillPreferences {
    override val enabled by prefs.enabled

    override var repetitions by prefs.repetitions

    override val resources by prefs.resources.map { set ->
        set.map {
            enumValueOf<RefillResourceEnum>(it)
        }.sortedBy { it.ordinal }
    }

    override val autoDecrement by prefs.autoDecrement

    override val shouldLimitRuns by prefs.shouldLimitRuns

    override val limitRuns by prefs.limitRuns
}