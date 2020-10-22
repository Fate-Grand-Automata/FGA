package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

interface IRefillPreferences {
    val enabled: Boolean
    var repetitions: Int
    val resources: List<RefillResourceEnum>
    val autoDecrement: Boolean

    val shouldLimitRuns: Boolean
    val limitRuns: Int

    val shouldLimitMats: Boolean
    val matToLimit: MaterialEnum
    val limitMats: Int
}