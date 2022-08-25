package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

interface IRefillPreferences {
    var repetitions: Int
    val resources: List<RefillResourceEnum>
    fun updateResources(resources: Set<RefillResourceEnum>)

    var shouldLimitRuns: Boolean
    var limitRuns: Int

    var shouldLimitMats: Boolean
    var limitMats: Int

    var shouldLimitCEs: Boolean
    var limitCEs: Int
}