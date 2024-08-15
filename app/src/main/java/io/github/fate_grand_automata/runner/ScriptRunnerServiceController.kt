package io.github.fate_grand_automata.runner

import android.app.Service
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.di.service.ServiceCoroutineScope
import io.github.fate_grand_automata.prefs.core.GameAreaMode
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.scripts.prefs.wantsMediaProjectionToken
import io.github.fate_grand_automata.util.DisplayHelper
import io.github.fate_grand_automata.util.ImageLoader
import io.github.fate_grand_automata.util.ScreenOffReceiver
import io.github.fate_grand_automata.util.ScriptMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ServiceScoped
class ScriptRunnerServiceController @Inject constructor(
    private val service: Service,
    @ApplicationContext private val context: Context,
    private val scriptManager: ScriptManager,
    private val screenshotServiceHolder: ScreenshotServiceHolder,
    private val imageLoader: ImageLoader,
    private val overlay: ScriptRunnerOverlay,
    private val prefs: IPreferences,
    private val prefsCore: PrefsCore,
    private val notification: ScriptRunnerNotification,
    private val displayHelper: DisplayHelper,
    private val messages: ScriptMessages,
    private val messageBox: ScriptRunnerMessageBox,
    @ServiceCoroutineScope private val scope: CoroutineScope
) {
    private val screenOffReceiver = ScreenOffReceiver()

    fun onDestroy() {
        Timber.i("Script runner service destroyed")

        scriptManager.stopScript()
        screenshotServiceHolder.close()

        imageLoader.clearImageCache()

        overlay.hide()
        scope.cancel()

        screenOffReceiver.unregister(service)
    }

    fun onCreate() {
        Timber.i("Script runner service created")

        screenOffReceiver.register(service) {
            Timber.v("SCREEN OFF")

            scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                if (success) {
                    val title = context.getString(R.string.script_paused)
                    val msg = context.getString(R.string.screen_turned_off)
                    messages.notify(msg)

                    scope.launch {
                        messageBox.show(title, msg)
                    }
                }
            }
        }

        val willAskForToken = prefs.wantsMediaProjectionToken
                && ScriptRunnerService.mediaProjectionToken == null

        if (!willAskForToken) {
            if (shouldDisplayPlayButton()) {
                overlay.show()
            }

            screenshotServiceHolder.prepareScreenshotService()
        }
    }

    fun onStart() {
        Timber.i("Script runner service started")

        notification.show(prefs.useRootForScreenshots)
    }

    fun onScreenConfigChanged() {
        if (shouldDisplayPlayButton()) {
            overlay.show()
        } else {
            overlay.hide()

            // Pause if script is running
            scope.launch {
                // This delay is to avoid race-condition with screen turn OFF listener
                delay(1.seconds)

                scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                    if (success) {
                        val msg = context.getString(R.string.script_paused)
                        messages.toast(msg)
                    }
                }
            }
        }
    }

    private fun shouldDisplayPlayButton(): Boolean {
        val isLandscape = displayHelper.metrics.let { it.widthPixels >= it.heightPixels }

        Timber.v(if (isLandscape) "LANDSCAPE" else "PORTRAIT")

        // Hide overlay in Portrait orientation (unless Surface Duo)
        return isLandscape || prefsCore.gameAreaMode.get() == GameAreaMode.Duo
    }

    fun onNewMediaProjectionToken() {
        screenshotServiceHolder.prepareScreenshotService()

        if (shouldDisplayPlayButton()) {
            overlay.show()
        }
    }
}