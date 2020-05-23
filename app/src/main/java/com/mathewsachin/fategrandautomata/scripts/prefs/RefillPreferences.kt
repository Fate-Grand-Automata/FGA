package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

class RefillPreferences {
    val enabled get() = getBoolPref(R.string.pref_refill_enabled)

    val repetitions get() = getStringAsIntPref(R.string.pref_refill_repetitions)

    val resource get() = getEnumPref(R.string.pref_refill_resource, RefillResourceEnum.AllApples)
}