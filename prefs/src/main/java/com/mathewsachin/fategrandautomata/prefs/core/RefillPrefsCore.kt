package com.mathewsachin.fategrandautomata.prefs.core

import com.mathewsachin.fategrandautomata.prefs.R
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum

class RefillPrefsCore(maker: PrefMaker) {
    val enabled = maker.bool(R.string.pref_refill_enabled)

    val repetitions = maker.stringAsInt(R.string.pref_refill_repetitions)

    val resources = maker.stringSet(R.string.pref_refill_resource)

    val autoDecrement = maker.bool(R.string.pref_refill_decrement)

    val shouldLimitRuns = maker.bool(R.string.pref_should_limit_runs)

    val limitRuns = maker.stringAsInt(R.string.pref_limit_runs, 1)

    val shouldLimitMats = maker.bool(R.string.pref_should_limit_mats)

    val matToLimit = maker.enum(R.string.pref_limit_mat_by, MaterialEnum.Heart)

    val limitMats = maker.stringAsInt(R.string.pref_limit_mats, 1)
}