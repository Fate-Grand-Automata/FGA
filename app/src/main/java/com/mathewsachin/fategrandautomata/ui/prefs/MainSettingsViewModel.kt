package com.mathewsachin.fategrandautomata.ui.prefs

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
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

    val useRootForScreenshots = prefsCore
        .useRootForScreenshots
        .asFlow()
        .asLiveData()

    val refillRepetitions = prefsCore
        .refill
        .repetitions
        .asFlow()
        .asLiveData()

    val shouldLimitRuns = prefsCore
        .refill
        .shouldLimitRuns
        .asFlow()
        .asLiveData()

    val limitRuns = prefsCore
        .refill
        .limitRuns
        .asFlow()
        .asLiveData()

    val shouldLimitMats = prefsCore
        .refill
        .shouldLimitMats
        .asFlow()
        .asLiveData()

    val limitMats = prefsCore
        .refill
        .limitMats
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

    val serviceStarted get() = ScriptRunnerService.serviceStarted

    // Activity context is needed since we can't show AlertDialog with Application context.
    fun ensureRootDir(picker: ActivityResultLauncher<Uri>, activityContext: Context): Boolean {
        val dirRoot = prefsCore.dirRoot.get()

        if (dirRoot.isBlank()) {
            AlertDialog.Builder(activityContext)
                .setTitle(R.string.p_choose_folder_title)
                .setMessage(R.string.p_choose_folder_message)
                .setPositiveButton(R.string.p_choose_folder_action) { _, _ ->
                    picker.launch(Uri.EMPTY)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            return false
        }

        val docFile = DocumentFile.fromTreeUri(context, Uri.parse(dirRoot))

        if (docFile?.exists() != true) {
            AlertDialog.Builder(activityContext)
                .setTitle(R.string.p_choose_folder_not_exist_title)
                .setMessage(R.string.p_choose_folder_not_exist_message)
                .setPositiveButton(R.string.p_choose_folder_action) { _, _ ->
                    picker.launch(Uri.EMPTY)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            return false
        }

        return true
    }
}