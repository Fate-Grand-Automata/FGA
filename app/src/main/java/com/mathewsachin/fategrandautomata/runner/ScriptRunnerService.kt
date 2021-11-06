package com.mathewsachin.fategrandautomata.runner

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.GameAreaMode
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.util.DisplayHelper
import com.mathewsachin.fategrandautomata.util.ImageLoader
import com.mathewsachin.fategrandautomata.util.ScreenOffReceiver
import com.mathewsachin.fategrandautomata.util.ScriptMessages
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.info
import timber.log.verbose
import javax.inject.Inject
import kotlin.time.Duration

@AndroidEntryPoint
class ScriptRunnerService: Service() {
    companion object {
        private val mServiceStarted = mutableStateOf(false)
        val serviceStarted: State<Boolean> = mServiceStarted

        private var instance: ScriptRunnerService? = null
            set(value) {
                field = value
                mServiceStarted.value = value != null
            }

        fun startService(context: Context) {
            val intent = makeServiceIntent(context)

            ContextCompat.startForegroundService(context, intent)
        }

        private fun makeServiceIntent(context: Context) =
            Intent(context, ScriptRunnerService::class.java)

        fun stopService(context: Context): Boolean {
            val intent = makeServiceIntent(context)
            return context.stopService(intent)
        }

        var mediaProjectionToken: Intent? = null
            set(value) {
                field = value
                instance?.screenshotServiceHolder?.prepareScreenshotService()
            }
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var prefsCore: PrefsCore

    @Inject
    lateinit var overlay: ScriptRunnerOverlay

    @Inject
    lateinit var scriptManager: ScriptManager

    @Inject
    lateinit var notification: ScriptRunnerNotification

    @Inject
    lateinit var messages: ScriptMessages

    @Inject
    lateinit var messageBox: ScriptRunnerMessageBox

    @Inject
    lateinit var screenshotServiceHolder: ScreenshotServiceHolder

    @Inject
    lateinit var displayHelper: DisplayHelper

    private val screenOffReceiver = ScreenOffReceiver()

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onDestroy() {
        Timber.info { "Script runner service destroyed" }

        scriptManager.stopScript()
        screenshotServiceHolder.close()

        imageLoader.clearImageCache()

        overlay.hide()

        screenOffReceiver.unregister(this)
        instance = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        Timber.info { "Script runner service created" }
        super.onCreate()
        instance = this
        notification.show()

        screenOffReceiver.register(this) {
            Timber.verbose { "SCREEN OFF" }

            scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                if (success) {
                    val title = getString(R.string.script_paused)
                    val msg = getString(R.string.screen_turned_off)
                    messages.notify(msg)

                    scope.launch {
                        messageBox.show(title, msg)
                    }
                }
            }
        }

        if (shouldDisplayPlayButton()) {
            overlay.show()
        }

        screenshotServiceHolder.prepareScreenshotService()
    }

    private fun shouldDisplayPlayButton(): Boolean {
        val isLandscape = displayHelper.metrics.let { it.widthPixels >= it.heightPixels }

        Timber.verbose { if (isLandscape) "LANDSCAPE" else "PORTRAIT" }

        // Hide overlay in Portrait orientation (unless Surface Duo)
        return isLandscape || prefsCore.gameAreaMode.get() == GameAreaMode.Duo
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (shouldDisplayPlayButton()) {
            overlay.show()
        } else {
            overlay.hide()

            // Pause if script is running
            scope.launch {
                // This delay is to avoid race-condition with screen turn OFF listener
                delay(Duration.seconds(1))

                scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                    if (success) {
                        val msg = getString(R.string.script_paused)
                        messages.toast(msg)
                    }
                }
            }
        }
    }
}
