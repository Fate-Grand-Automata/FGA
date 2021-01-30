package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.prefs.core.Pref
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

    // Have to keep current slider value independent of the preference to prevent inconsistencies
    private val fineTuneStates = mutableMapOf<Pref<Int>, MutableState<Float>>()

    fun getState(pref: Pref<Int>) =
        fineTuneStates.getOrPut(pref) {
            mutableStateOf(pref.get().toFloat())
        }

    fun resetAll() =
        fineTunePrefs.forEach {
            it.resetToDefault()
            getState(it).value = it.defaultValue.toFloat()
        }
}