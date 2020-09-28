package com.mathewsachin.fategrandautomata.prefs.core

import com.mathewsachin.fategrandautomata.prefs.R

class RefillPrefsCore(maker: PrefMaker) {
    val enabled = maker.bool(R.string.pref_refill_enabled)

    val repetitions = maker.stringAsInt(R.string.pref_refill_repetitions)

    val resources = maker.stringSet(R.string.pref_refill_resource)

    val autoDecrement = maker.bool(R.string.pref_refill_decrement)

    val shouldLimitRuns = maker.bool(R.string.pref_should_limit_runs)

    val limitRuns = maker.stringAsInt(R.string.pref_limit_runs, 1)
}