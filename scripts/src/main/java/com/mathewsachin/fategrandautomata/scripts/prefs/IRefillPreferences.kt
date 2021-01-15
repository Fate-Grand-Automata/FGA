package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

interface IRefillPreferences {
    val enabled: Boolean
    var repetitions: Int
    val resources: List<RefillResourceEnum>
    val autoDecrement: Boolean

    val shouldLimitRuns: Boolean
    var limitRuns: Int
    val autoDecrementRuns: Boolean

    val shouldLimitMats: Boolean
    var limitMats: Int
    val autoDecrementMats: Boolean
}