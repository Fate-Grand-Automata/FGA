package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageButton
import android.widget.Toast
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.root.SuperUser
import com.mathewsachin.fategrandautomata.util.clearImageCache
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.fategrandautomata.util.AndroidImpl
import com.mathewsachin.fategrandautomata.util.ScreenOffReceiver
import com.mathewsachin.fategrandautomata.util.ScriptManager
import com.mathewsachin.fategrandautomata.util.setThrottledClickListener
import com.mathewsachin.libautomata.*

class ScriptRunnerService : AccessibilityService() {
    companion object {
        var Instance: ScriptRunnerService? = null
    }

    private lateinit var userInterface: ScriptRunnerUserInterface
    private lateinit var scriptManager: ScriptManager
    private var sshotService: IScreenshotService? = null
    private val screenOffReceiver = ScreenOffReceiver()

    // stopping is handled by Screenshot service
    private var mediaProjection: MediaProjection? = null

    val notification = ScriptRunnerNotification(this)

    lateinit var mediaProjectionManager: MediaProjectionManager
        private set

    override fun onUnbind(intent: Intent?): Boolean {
        stop()

        unregisterGestures()

        screenOffReceiver.unregister(this)
        Instance = null

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Instance = null
    }

    val wantsMediaProjectionToken: Boolean get() = !Preferences.useRootForScreenshots

    var serviceStarted = false
        private set

    fun start(MediaProjectionToken: Intent? = null): Boolean {
        if (serviceStarted) {
            return false
        }

        if (!registerScreenshot(MediaProjectionToken)) {
            return false
        }

        userInterface.show()

        serviceStarted = true

        return true
    }

    private fun registerScreenshot(MediaProjectionToken: Intent?): Boolean {
        sshotService = try {
            if (MediaProjectionToken != null) {
                mediaProjection =
                    mediaProjectionManager.getMediaProjection(RESULT_OK, MediaProjectionToken)
                MediaProjectionScreenshotService(
                    mediaProjection!!,
                    userInterface.mediaProjectionMetrics
                )
            } else RootScreenshotService(SuperUser())
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }

        ScreenshotManager.register(sshotService ?: return false)
        return true
    }

    fun stop(): Boolean {
        scriptManager.stopScript()

        if (!serviceStarted) {
            return false
        }

        sshotService?.close()
        sshotService = null

        ScreenshotManager.releaseMemory()
        clearImageCache()

        userInterface.hide()
        serviceStarted = false

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
        val alarmService = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )

        super.onTaskRemoved(rootIntent)
    }

    fun registerScriptCtrlBtnListeners(scriptCtrlBtn: ImageButton) {
        scriptCtrlBtn.setThrottledClickListener {
            if (scriptManager.scriptStarted) {
                scriptManager.stopScript()
            } else sshotService?.let {
                // Overwrite the server in the preferences with the detected one, if possible
                currentFgoServer?.let { server -> Preferences.gameServer = server }

                scriptManager.startScript(this, it)
            }
        }
    }

    override fun onServiceConnected() {
        Instance = this
        AutomataApi.registerPlatform(AndroidImpl(this))

        val gestureService = AccessibilityGestures(this)
        registerGestures(gestureService)

        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        userInterface = ScriptRunnerUserInterface(this)
        scriptManager = ScriptManager(userInterface)

        screenOffReceiver.register(this) {
            scriptManager.stopScript()
        }

        super.onServiceConnected()
    }

    override fun onInterrupt() {}

    private var currentFgoServer: GameServerEnum? = null

    /**
     * This method is called on any subscribed [AccessibilityEvent] in script_runner_service.xml.
     *
     * When the app in the foreground changes, this method will check if the foreground app is one
     * of the FGO APKs and will store that information into [currentFgoServer].
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val foregroundAppName = event.packageName.toString()

                GameServerEnum.fromPackageName(foregroundAppName)
                    ?.let { currentFgoServer = it }
            }
        }
    }

    fun showMessageBox(Title: String, Message: String, Error: Exception? = null) {
        ScriptRunnerDialog(userInterface).apply {
            setTitle(Title)
            setMessage(Message)
            setPositiveButton(getString(android.R.string.ok)) { }

            if (Error != null) {
                setNeutralButton("Copy") {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Error", Error.messageAndStackTrace)

                    clipboard.setPrimaryClip(clipData)
                }
            }

            show()
        }
    }
}
