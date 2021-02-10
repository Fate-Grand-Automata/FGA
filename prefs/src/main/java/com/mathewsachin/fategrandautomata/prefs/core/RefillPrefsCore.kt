package com.mathewsachin.fategrandautomata.prefs.core

class RefillPrefsCore(maker: PrefMaker) {
    val repetitions = maker.stringAsInt("refill_repetitions")
    val resources = maker.stringSet("refill_resource_x")

    val shouldLimitRuns = maker.bool("should_limit_runs")
    val limitRuns = maker.stringAsInt("limit_runs", 1)

    val shouldLimitMats = maker.bool("should_limit_mats")
    val limitMats = maker.stringAsInt("limit_mats", 1)
}