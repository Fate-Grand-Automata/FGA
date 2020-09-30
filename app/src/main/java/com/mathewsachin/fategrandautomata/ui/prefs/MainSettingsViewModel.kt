package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.stringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicBoolean

class MainSettingsViewModel @ViewModelInject constructor(
    val prefsCore: PrefsCore,
    val prefs: IPreferences,
    @ApplicationContext val context: Context
) : ViewModel() {
    val autoStartService
        get() =
            prefsCore.autoStartService.get() && oncePerActivityStart.getAndSet(false)

    private val oncePerActivityStart = AtomicBoolean(false)
    fun activityStarted() = oncePerActivityStart.set(true)

    val gameServer = prefsCore
        .gameServer
        .asFlow()
        .asLiveData()

    val scriptMode = prefsCore
        .scriptMode
        .asFlow()
        .asLiveData()

    val refillRepetitions = prefsCore
        .refill
        .repetitions
        .asFlow()
        .asLiveData()

    private val refillResourcesFlow = prefsCore
        .refill
        .resources
        .asFlow()
        .map {
            val resources = prefs.refill.resources

            if (resources.isNotEmpty()) {
                resources.joinToString(" > ") {
                    context.getString(it.stringRes)
                }
            } else context.getString(R.string.p_refill_none)
        }

    val refillResources = refillResourcesFlow.asLiveData()

    val refillMessage = combine(
        prefsCore.refill.enabled.asFlow(),
        prefsCore.refill.resources.asFlow(),
        prefsCore.refill.repetitions.asFlow(),
        refillResourcesFlow
    ) { enabled, resources, repetitions, refillResourcesMsg ->
        if (enabled && repetitions > 0 && resources.isNotEmpty())
            "[$refillResourcesMsg] x$repetitions"
        else context.getString(R.string.p_refill_off)
    }
        .asLiveData()
}