package com.mathewsachin.fategrandautomata.scripts.prefs

import kotlin.time.Duration

interface IGesturesPreferences {
    val clickWaitTime: Duration
    val clickDuration: Duration
    val clickDelay: Duration
    val swipeWaitTime: Duration
    val swipeDuration: Duration
}