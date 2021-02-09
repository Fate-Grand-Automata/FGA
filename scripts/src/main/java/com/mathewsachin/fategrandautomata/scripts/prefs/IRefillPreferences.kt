package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

interface IRefillPreferences {
    var enabled: Boolean
    var repetitions: Int
    val resources: List<RefillResourceEnum>
    fun updateResources(resources: Set<RefillResourceEnum>)

    val autoDecrement: Boolean

    var shouldLimitRuns: Boolean
    var limitRuns: Int
    val autoDecrementRuns: Boolean

    var shouldLimitMats: Boolean
    var limitMats: Int
    val autoDecrementMats: Boolean
}