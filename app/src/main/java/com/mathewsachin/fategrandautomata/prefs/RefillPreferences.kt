package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

class RefillPreferences(val prefs: SharedPreferenceDelegation) {
    val enabled by prefs.bool(R.string.pref_refill_enabled)

    val repetitions by prefs.stringAsInt(R.string.pref_refill_repetitions)

    val resource by prefs.enum(R.string.pref_refill_resource, RefillResourceEnum.AllApples)
}