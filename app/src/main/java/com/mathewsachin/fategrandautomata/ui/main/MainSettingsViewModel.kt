package com.mathewsachin.fategrandautomata.ui.main

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.accessibility.ScriptRunnerService
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MainSettingsViewModel @Inject constructor(
    val prefsCore: PrefsCore,
    val prefs: IPreferences
) : ViewModel() {
    val autoStartService
        get() =
            prefsCore.autoStartService.get() && oncePerActivityStart.getAndSet(false)

    private val oncePerActivityStart = AtomicBoolean(false)
    fun activityStarted() = oncePerActivityStart.set(true)

    val serviceStarted get() = ScriptRunnerService.serviceStarted

    // Activity context is needed since we can't show AlertDialog with Application context.
    fun ensureRootDir(picker: ActivityResultLauncher<Uri>, context: Context): Boolean {
        val dirRoot = prefsCore.dirRoot.get()

        if (dirRoot.isBlank()) {
            AlertDialog.Builder(context)
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
            AlertDialog.Builder(context)
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