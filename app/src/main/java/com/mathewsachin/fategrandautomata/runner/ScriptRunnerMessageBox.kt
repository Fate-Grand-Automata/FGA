package com.mathewsachin.fategrandautomata.runner

import android.app.Service
import android.content.ClipboardManager
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.util.set
import com.mathewsachin.fategrandautomata.util.showOverlayDialog
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

@ServiceScoped
class ScriptRunnerMessageBox @Inject constructor(
    private val service: Service,
    private val clipboardManager: ClipboardManager,
    private val notification: ScriptRunnerNotification
) {
    suspend fun show(
        title: String,
        message: String,
        error: Exception? = null,
    ) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                showOverlayDialog(service) {
                    setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setOnDismissListener {
                            notification.hideMessage()
                            continuation.resume(true)
                        }
                        .let {
                            if (error != null) {
                                it.setNeutralButton(R.string.unexpected_error_copy) { _, _ ->
                                    clipboardManager.set(context, error)
                                }
                            }
                        }
                }
            }
        }
    }
}