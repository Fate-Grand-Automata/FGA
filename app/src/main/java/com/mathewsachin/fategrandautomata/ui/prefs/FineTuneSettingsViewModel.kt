package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import javax.inject.Inject

class FineTuneSettingsViewModel @Inject constructor(
    val prefs: PrefsCore
) : ViewModel() {
    val fineTunePrefs = listOf(
        prefs.supportSwipesPerUpdate,
        prefs.supportMaxUpdates,
        prefs.minSimilarity,
        prefs.mlbSimilarity,
        prefs.clickWaitTime,
        prefs.clickDuration,
        prefs.clickDelay,
        prefs.swipeWaitTime,
        prefs.swipeDuration,
        prefs.supportSwipeMultiplier,
        prefs.skillDelay,
        prefs.waitMultiplier
    )

    val liveDataMap = fineTunePrefs
        .associate { it.key to it.asFlow().asLiveData() }

    fun resetAll() = fineTunePrefs.forEach { it.resetToDefault() }
}