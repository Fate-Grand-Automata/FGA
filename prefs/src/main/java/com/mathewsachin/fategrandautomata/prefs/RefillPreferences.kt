package com.mathewsachin.fategrandautomata.prefs

import com.mathewsachin.fategrandautomata.prefs.helpers.SharedPreferenceDelegation
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IRefillPreferences

internal class RefillPreferences(val prefs: SharedPreferenceDelegation) :
    IRefillPreferences {
    override val enabled by prefs.bool(R.string.pref_refill_enabled)

    override val repetitions by prefs.stringAsInt(R.string.pref_refill_repetitions)

    override val resource by prefs.enum(R.string.pref_refill_resource, RefillResourceEnum.AllApples)

    override val shouldLimitRuns by prefs.bool(R.string.pref_should_limit_runs)

    override val limitRuns by prefs.int(R.string.pref_limit_runs, 1)
}