package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.media.projection.MediaProjectionManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.di.script.ScriptComponentBuilder
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.root.SuperUser
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.util.*
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.libautomata.messageAndStackTrace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.debug
import timber.log.info
import timber.log.verbose
import javax.inject.Inject
import kotlin.time.seconds

@AndroidEntryPoint
class ScriptRunnerService : AccessibilityService() {
    companion object {
        var Instance: ScriptRunnerService? = null
            private set

        fun isAccessibilityServiceRunning() = Instance != null

        fun isServiceStarted() =
            Instance?.serviceState is ServiceState.Started

        private val _serviceStarted = MutableLiveData(isServiceStarted())
        val serviceStarted: LiveData<Boolean> = _serviceStarted

        fun startService(mediaProjectionToken: Intent? = null): Boolean {
            return Instance?.let { service ->
                service.start(mediaProjectionToken).also { success ->
                    if (success) {
                        _serviceStarted.value = true

                        val msg = service.getString(R.string.start_service_toast)
                        service.platformImpl.toast(msg)
                    }
                }
            } ?: false
        }

        fun stopService(): Boolean {
            return (Instance?.stop() == true).also {
                _serviceStarted.value = false
            }
        }
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var prefs: IPreferences

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

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
    lateinit var alarmManager: AlarmManager

    @Inject
    lateinit var clipboardManager: ClipboardManager

    private val screenOffReceiver = ScreenOffReceiver()

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.info { "Accessibility Service unbind" }

        stop()

        screenOffReceiver.unregister(this)
        Instance = null

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Instance = null
    }

    val wantsMediaProjectionToken: Boolean
        get() = !(RootScreenshotService.canUseRootForScreenshots() && prefs.useRootForScreenshots)

    var serviceState: ServiceState = ServiceState.Stopped
        private set

    private fun start(MediaProjectionToken: Intent? = null): Boolean {
        if (serviceState is ServiceState.Started) {
            return false
        }

        val screenshotService = registerScreenshot(MediaProjectionToken)
            ?: return false

        serviceState = ServiceState.Started(screenshotService)

        if (isLandscape()) {
            userInterface.show()
        }

        return true
    }

    private fun registerScreenshot(MediaProjectionToken: Intent?): IScreenshotService? {
        return try {
            if (MediaProjectionToken != null) {
                val mediaProjection =
                    mediaProjectionManager.getMediaProjection(RESULT_OK, MediaProjectionToken)
                MediaProjectionScreenshotService(
                    mediaProjection!!,
                    userInterface.mediaProjectionMetrics,
                    storageProvider
                )
            } else RootScreenshotService(SuperUser(), storageProvider, platformImpl)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun stop(): Boolean {
        scriptManager.stopScript()

        serviceState.let {
            if (it is ServiceState.Started) {
                it.screenshotService.close()
            } else return false
        }

        imageLoader.clearImageCache()

        userInterface.hide()
        serviceState = ServiceState.Stopped

        notification.hide()

        return true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // from https://stackoverflow.com/a/43310945/5971497

        val restartServiceIntent = Intent(applicationContext, javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )

        super.onTaskRemoved(rootIntent)
    }

    fun registerScriptCtrlBtnListeners(scriptCtrlBtn: ImageButton) {
        scriptCtrlBtn.setOnClickListener {
            val state = serviceState

            if (state is ServiceState.Started) {
                when (scriptManager.scriptState) {
                    is ScriptState.Started -> scriptManager.stopScript()
                    is ScriptState.Stopped -> {
                        scriptManager.startScript(this, state.screenshotService, scriptComponentBuilder)
                    }
                    is ScriptState.Stopping -> {
                        Timber.debug { "Already stopping ..." }
                    }
                }
            }
        }
    }

    fun registerScriptPauseBtnListeners(scriptPauseBtn: ImageButton) =
        scriptPauseBtn.setOnClickListener {
            scriptManager.pause(ScriptManager.PauseAction.Toggle)
        }

    override fun onServiceConnected() {
        Timber.info { "Accessibility Service bound to system" }

        // We only want events from FGO
        serviceInfo = serviceInfo.apply {
            packageNames = GameServerEnum
                .values()
                .flatMap { it.packageNames.toList() }
                .toTypedArray()
        }

        Instance = this

        screenOffReceiver.register(this) {
            Timber.verbose { "SCREEN OFF" }

            scriptManager.pause(ScriptManager.PauseAction.Pause).let { success ->
                if (success) {
                    val title = getString(R.string.script_paused)
                    val msg = getString(R.string.screen_turned_off)
                    platformImpl.notify(msg)
                    platformImpl.messageBox(title, msg)
                }
            }
        }

        super.onServiceConnected()
    }

    override fun onInterrupt() {}

    private fun isLandscape() =
        userInterface.metrics.let { it.widthPixels >= it.heightPixels }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // Hide overlay in Portrait orientation
        if (isLandscape()) {
            Timber.verbose { "LANDSCAPE" }

            if (serviceState is ServiceState.Started) {
                userInterface.show()
            }
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

    /**
     * This method is called on any subscribed [AccessibilityEvent] in script_runner_service.xml.
     *
     * When the app in the foreground changes, this method will check if the foreground app is one
     * of the FGO APKs.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val foregroundAppName = event.packageName?.toString()
                    ?: return

                GameServerEnum.fromPackageName(foregroundAppName)?.let { server ->
                    Timber.debug { "Detected FGO: $server" }

                    prefs.gameServer = server
                }
            }
        }
    }

    fun showMessageBox(Title: String, Message: String, Error: Exception?, onDismiss: () -> Unit) {
        showOverlayDialog(this) {
            setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setOnDismissListener {
                    notification.hideMessage()
                    onDismiss()
                }
                .let {
                    if (Error != null) {
                        // TODO: Translate
                        it.setNeutralButton("Copy") { _, _ ->
                            val clipData = ClipData.newPlainText("Error", Error.messageAndStackTrace)

                            clipboardManager.setPrimaryClip(clipData)
                        }
                    }
                }
        }
    }
}
