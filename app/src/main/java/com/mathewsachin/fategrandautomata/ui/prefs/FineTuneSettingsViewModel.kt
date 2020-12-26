package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore

class FineTuneSettingsViewModel @ViewModelInject constructor(
    val prefs: PrefsCore
) : ViewModel() {
    val fineTunePrefs = listOf(
        prefs.supportSwipesPerUpdate,
        prefs.supportMaxUpdates,
        prefs.minSimilarity,
        prefs.mlbSimilarity,
        prefs.stageCounterSimilarity,
        prefs.clickWaitTime,
        prefs.clickDuration,
        prefs.clickDelay,
        prefs.swipeWaitTime,
        prefs.swipeDuration,
        prefs.swipeMultiplier,
        prefs.skillDelay,
        prefs.waitMultiplier,
        prefs.waitBeforeTurn,
        prefs.waitBeforeCards
    )

    val liveDataMap = fineTunePrefs
        .associate { it.key to it.asFlow().asLiveData() }

    fun resetAll() = fineTunePrefs.forEach { it.resetToDefault() }
}