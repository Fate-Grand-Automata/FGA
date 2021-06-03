package com.mathewsachin.fategrandautomata.accessibility

import android.app.Activity
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.widget.ImageButton
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.root.SuperUser
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.wantsMediaProjectionToken
import com.mathewsachin.fategrandautomata.util.*
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.messageAndStackTrace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.*
import javax.inject.Inject
import kotlin.time.seconds

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
                instance?.prepareScreenshotService()
            }
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var prefs: IPreferences

    @Inject
    lateinit var prefsCore: PrefsCore

    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var userInterface: ScriptRunnerUserInterface

    @Inject
    lateinit var scriptManager: ScriptManager

    @Inject
    lateinit var notification: ScriptRunnerNotification

    @Inject
    lateinit var platformImpl: IPlatformImpl

    @Inject
    lateinit var scriptComponentBuilder: ScriptComponentBuilder

    @Inject
    lateinit var clipboardManager: ClipboardManager

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    private val screenOffReceiver = ScreenOffReceiver()

    private var screenshotService: IScreenshotService? = null

    override fun onDestroy() {
        Timber.info { "Script runner service destroyed" }

        scriptManager.stopScript()
        screenshotService?.close()
        screenshotService = null

        imageLoader.clearImageCache()

        userInterface.hide()

        screenOffReceiver.unregister(this)
        instance = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun registerScriptCtrlBtnListeners(scriptCtrlBtn: ImageButton) {
        scriptCtrlBtn.setOnClickListener {
            when (scriptManager.scriptState) {
                is ScriptState.Started -> scriptManager.stopScript()
                is ScriptState.Stopped -> {
                    updateGameServer()

                    screenshotService?.let {
                        scriptManager.startScript(this, it, scriptComponentBuilder)
                    }
                }
                is ScriptState.Stopping -> {
                    Timber.debug { "Already stopping ..." }
                }
            }
        }
    }

    private fun updateGameServer() {
        val server = prefsCore.gameServerRaw.get()

        prefs.gameServer =
            if (server == PrefsCore.GameServerAutoDetect)
                (TapperService.instance?.detectedFgoServer ?: GameServerEnum.En).also {
                    Timber.debug { "Using auto-detected Game Server: $it" }
                }
            else try {
                enumValueOf<GameServerEnum>(server).also {
                    Timber.debug { "Using Game Server: $it" }
                }
            } catch (e: Exception) {
                Timber.error(e) { "Game Server: Falling back to NA" }

                GameServerEnum.En
            }
    }

    fun registerScriptPauseBtnListeners(scriptPauseBtn: ImageButton) =
        scriptPauseBtn.setOnClickListener {
            scriptManager.pause(ScriptManager.PauseAction.Toggle)
        }

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
                    platformImpl.notify(msg)
                    showMessageBox(title, msg)
                }
            }
        }

        if (isLandscape()) {
            userInterface.show()
        }

        prepareScreenshotService()
    }

    private fun isLandscape() =
        userInterface.metrics.let { it.widthPixels >= it.heightPixels }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // Hide overlay in Portrait orientation
        if (isLandscape()) {
            Timber.verbose { "LANDSCAPE" }

            userInterface.show()
        } else {
            Timber.verbose { "PORTRAIT" }

            userInterface.hide()

            // Pause if script is running
            GlobalScope.launch {
                // This delay is to avoid race-condition with screen turn OFF listener
                delay(1.seconds)

                scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                    if (success) {
                        val msg = getString(R.string.script_paused)
                        platformImpl.toast(msg)
                    }
                }
            }
        }
    }

    fun showMessageBox(
        title: String,
        message: String,
        error: Exception? = null,
        onDismiss: () -> Unit = { }
    ) {
        showOverlayDialog(this) {
            setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setOnDismissListener {
                    notification.hideMessage()
                    onDismiss()
                }
                .let {
                    if (error != null) {
                        // TODO: Translate
                        it.setNeutralButton("Copy") { _, _ ->
                            val clipData = ClipData.newPlainText("Error", error.messageAndStackTrace)

                            clipboardManager.setPrimaryClip(clipData)
                        }
                    }
                }
        }
    }

    fun prepareScreenshotService() {
        screenshotService = try {
            if (prefs.wantsMediaProjectionToken) {
                // Cloning the Intent allows reuse.
                // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
                val token = mediaProjectionToken?.clone() as Intent

                val mediaProjection =
                    mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, token)
                MediaProjectionScreenshotService(
                    mediaProjection!!,
                    userInterface.mediaProjectionMetrics,
                    storageProvider
                )
            } else RootScreenshotService(SuperUser(), storageProvider)
        } catch (e: Exception) {
            Timber.error(e) { "Error preparing screenshot service" }
            null
        }
    }
}
