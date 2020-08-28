package com.mathewsachin.fategrandautomata.ui.prefs

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import kotlinx.coroutines.flow.combine
import java.util.concurrent.atomic.AtomicBoolean

class MainSettingsViewModel @ViewModelInject constructor(
    val prefs: PrefsCore
) : ViewModel() {
    val autoStartService
        get() =
            prefs.autoStartService.get() && oncePerActivityStart.getAndSet(false)

    private val oncePerActivityStart = AtomicBoolean(false)
    fun activityStarted() = oncePerActivityStart.set(true)

    val gameServer = prefs
        .gameServer
        .asFlow()
        .asLiveData()

    val refillRepetitions = prefs
        .refill
        .repetitions
        .asFlow()
        .asLiveData()

    val refillMessage = combine(
        prefs.refill.enabled.asFlow(),
        prefs.refill.resource.asFlow(),
        prefs.refill.repetitions.asFlow()
    ) { enabled, resource, repetitions ->
        if (enabled)
            "$resource x${repetitions}"
        else "OFF"
    }
        .asLiveData()
}