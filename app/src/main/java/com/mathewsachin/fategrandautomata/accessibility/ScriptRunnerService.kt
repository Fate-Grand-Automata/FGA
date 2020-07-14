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
import com.mathewsachin.fategrandautomata.scripts.clearImageCache
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
    private var superUser: SuperUser? = null
    private val screenOffReceiver = ScreenOffReceiver()

    // stopping is handled by Screenshot service
    private var mediaProjection: MediaProjection? = null

    private fun getSuperUser(): SuperUser {
        return (superUser ?: SuperUser()).also {
            superUser = it
        }
    }

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

    val wantsMediaProjectionToken: Boolean get() = !Preferences.UseRootForScreenshots

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
                MediaProjectionScreenshotService(mediaProjection!!, userInterface.mediaProjectionMetrics)
            } else RootScreenshotService(
                getSuperUser()
            )
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

        superUser?.close()
        superUser = null

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
            } else scriptManager.startScript(this, mediaProjection)
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

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
