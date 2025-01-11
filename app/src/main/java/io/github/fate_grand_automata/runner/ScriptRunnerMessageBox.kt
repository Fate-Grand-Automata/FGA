package io.github.fate_grand_automata.runner

import android.content.Context
import android.content.ClipboardManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.util.set
import io.github.fate_grand_automata.util.showOverlayDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

@ServiceScoped
class ScriptRunnerMessageBox @Inject constructor(
    @ApplicationContext private val context: Context,
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
                showOverlayDialog(context) {
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