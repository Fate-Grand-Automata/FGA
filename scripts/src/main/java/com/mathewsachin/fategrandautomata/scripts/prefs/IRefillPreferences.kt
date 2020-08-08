package com.mathewsachin.fategrandautomata.scripts.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum

interface IRefillPreferences {
    val enabled: Boolean
    val repetitions: Int
    val resource: RefillResourceEnum

    val shouldLimitRuns: Boolean
    val limitRuns: Int
}