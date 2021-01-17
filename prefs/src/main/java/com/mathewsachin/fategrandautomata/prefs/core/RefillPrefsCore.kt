package com.mathewsachin.fategrandautomata.prefs.core

class RefillPrefsCore(maker: PrefMaker) {
    val enabled = maker.bool("refill_enabled")
    val repetitions = maker.stringAsInt("refill_repetitions")
    val resources = maker.stringSet("refill_resource_x")
    val autoDecrement = maker.bool("refill_decrement")

    val shouldLimitRuns = maker.bool("should_limit_runs")
    val limitRuns = maker.stringAsInt("limit_runs", 1)
    val autoDecrementRuns = maker.bool("limit_runs_decrement")

    val shouldLimitMats = maker.bool("should_limit_mats")
    val limitMats = maker.stringAsInt("limit_mats", 1)
    val autoDecrementMats = maker.bool("limit_mats_decrement")
}