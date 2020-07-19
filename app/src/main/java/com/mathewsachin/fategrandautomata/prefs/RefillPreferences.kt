package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IRefillPreferences

class RefillPreferences(val prefs: SharedPreferenceDelegation) :
    IRefillPreferences {
    override val enabled by prefs.bool(R.string.pref_refill_enabled)

    override val repetitions by prefs.stringAsInt(R.string.pref_refill_repetitions)

    override val resource by prefs.enum(R.string.pref_refill_resource, RefillResourceEnum.AllApples)
}