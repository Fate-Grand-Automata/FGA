package com.mathewsachin.fategrandautomata.ui.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.StorageProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import timber.log.info
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val prefs: IPreferences,
    val storageProvider: StorageProvider
) : ViewModel() {
    val autoStartService
        get() =
            prefsCore.autoStartService.get()
                    && oncePerActivityStart.getAndSet(false)
                    && !ScriptRunnerService.serviceStarted.value

    private val oncePerActivityStart = AtomicBoolean(false)
    fun activityStarted() = oncePerActivityStart.set(true)

    // Activity context is needed since we can't show AlertDialog with Application context.
    fun ensureRootDir(context: Context, pickDirectory: () -> Unit): Boolean {
        val dirRoot = prefsCore.dirRoot.get()

        if (dirRoot.isBlank()) {
            AlertDialog.Builder(context)
                .setTitle(R.string.p_choose_folder_title)
                .setMessage(R.string.p_choose_folder_message)
                .setPositiveButton(R.string.p_choose_folder_action) { _, _ ->
                    pickDirectory()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            return false
        }

        val docFile = DocumentFile.fromTreeUri(context, Uri.parse(dirRoot))

        if (docFile?.exists() != true) {
            AlertDialog.Builder(context)
                .setTitle(R.string.p_choose_folder_not_exist_title)
                .setMessage(R.string.p_choose_folder_not_exist_message)
                .setPositiveButton(R.string.p_choose_folder_action) { _, _ ->
                    pickDirectory()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            return false
        }

        return true
    }

    fun onStartMediaProjectionResult(context: Context, intent: Intent?) {
        if (intent == null) {
            Timber.info { "MediaProjection cancelled by user" }
            ScriptRunnerService.stopService(context)
        } else {
            ScriptRunnerService.mediaProjectionToken = intent
        }
    }
}