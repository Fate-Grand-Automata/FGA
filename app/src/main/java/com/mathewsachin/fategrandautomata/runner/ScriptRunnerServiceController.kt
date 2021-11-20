package com.mathewsachin.fategrandautomata.runner

import android.app.Service
import android.content.Context
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.di.service.ServiceCoroutineScope
import com.mathewsachin.fategrandautomata.prefs.core.GameAreaMode
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.util.*
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.info
import timber.log.verbose
import javax.inject.Inject
import kotlin.time.Duration

@ServiceScoped
class ScriptRunnerServiceController @Inject constructor(
    private val service: Service,
    @ApplicationContext private val context: Context,
    private val scriptManager: ScriptManager,
    private val screenshotServiceHolder: ScreenshotServiceHolder,
    private val imageLoader: ImageLoader,
    private val overlay: ScriptRunnerOverlay,
    private val prefsCore: PrefsCore,
    private val notification: ScriptRunnerNotification,
    private val displayHelper: DisplayHelper,
    private val messages: ScriptMessages,
    private val messageBox: ScriptRunnerMessageBox,
    @ServiceCoroutineScope private val scope: CoroutineScope
) {
    private val screenOffReceiver = ScreenOffReceiver()

    fun onDestroy() {
        Timber.info { "Script runner service destroyed" }

        scriptManager.stopScript()
        screenshotServiceHolder.close()

        imageLoader.clearImageCache()

        overlay.hide()
        scope.cancel()

        screenOffReceiver.unregister(service)
    }

    fun onCreate() {
        Timber.info { "Script runner service created" }
        notification.show()

        screenOffReceiver.register(service) {
            Timber.verbose { "SCREEN OFF" }

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
    }

    fun onScreenConfigChanged() {
        refreshPlayButton()
    }

    fun refreshPlayButton() {
        if (shouldDisplayPlayButton()) {
            overlay.show()
        } else {
            overlay.hide()

            if (scriptManager.scriptState is ScriptState.Started) {
                // Pause if script is running
                scope.launch {
                    // This delay is to avoid race-condition with screen turn OFF listener
                    delay(Duration.seconds(1))

                    scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                        if (success) {
                            val msg = context.getString(R.string.script_paused)
                            messages.toast(msg)
                        }
                    }
                }
            }
        }
    }

    private fun shouldDisplayPlayButton(): Boolean {
        val isLandscape = displayHelper.metrics.let { it.widthPixels >= it.heightPixels }

        Timber.verbose { if (isLandscape) "LANDSCAPE" else "PORTRAIT" }

        // Hide overlay in Portrait orientation (unless Surface Duo)
        return isLandscape || prefsCore.gameAreaMode.get() == GameAreaMode.Duo
    }
}