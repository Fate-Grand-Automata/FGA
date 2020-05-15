package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.postDelayed
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionRecording
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.root.RootGestures
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.root.SuperUser
import com.mathewsachin.fategrandautomata.scripts.clearImageCache
import com.mathewsachin.fategrandautomata.scripts.clearSupportCache
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoFriendGacha
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoLottery
import com.mathewsachin.fategrandautomata.scripts.entrypoints.SupportImageMaker
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.fategrandautomata.ui.MainActivity
import com.mathewsachin.fategrandautomata.ui.highlightView
import com.mathewsachin.fategrandautomata.ui.support_img_namer.SupportImageIdKey
import com.mathewsachin.fategrandautomata.ui.support_img_namer.SupportImageNamerActivity
import com.mathewsachin.fategrandautomata.util.AndroidImpl
import kotlin.math.roundToInt

class ScriptRunnerService : AccessibilityService() {
    companion object {
        var Instance: ScriptRunnerService? = null
    }

    private lateinit var layout: FrameLayout
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var highlightLayoutParams: WindowManager.LayoutParams
    private lateinit var scriptCtrlBtn: ImageButton
    private val metrics = DisplayMetrics()
    private var sshotService: IScreenshotService? = null
    private var gestureService: IGestureService? = null
    private var superUser: SuperUser? = null

    // stopping is handled by Screenshot service
    private var mediaProjection: MediaProjection? = null
    private var recording: MediaProjectionRecording? = null

    private fun getSuperUser(): SuperUser {
        return (superUser ?: SuperUser()).also {
            superUser = it
        }
    }

    lateinit var mediaProjectionManager: MediaProjectionManager
        private set

    override fun onUnbind(intent: Intent?): Boolean {
        stop()

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

    private var scriptStarted = false

    fun start(MediaProjectionToken: Intent? = null): Boolean {
        if (serviceStarted) {
            return false
        }

        if (!registerScreenshot(MediaProjectionToken)) {
            return false
        }

        if (!registerGestures()) {
            return false
        }

        windowManager.addView(highlightView, highlightLayoutParams)
        windowManager.addView(layout, layoutParams)

        serviceStarted = true

        return true
    }

    private fun registerScreenshot(MediaProjectionToken: Intent?): Boolean {
        sshotService = try {
            if (MediaProjectionToken != null) {
                mediaProjection =
                    mediaProjectionManager.getMediaProjection(RESULT_OK, MediaProjectionToken)
                MediaProjectionScreenshotService(mediaProjection!!, metrics)
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

    private fun registerGestures(): Boolean {
        gestureService = try {
            if (Preferences.UseRootForGestures) {
                RootGestures(getSuperUser())
            } else AccessibilityGestures(this)
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            return false
        }

        AutomataApi.registerGestures(gestureService ?: return false)
        return true
    }

    fun stop(): Boolean {
        stopScript()

        if (!serviceStarted) {
            return false
        }

        sshotService?.close()
        sshotService = null

        gestureService?.close()
        gestureService = null

        superUser?.close()
        superUser = null

        ScreenshotManager.releaseMemory()
        clearImageCache()

        windowManager.removeView(layout)
        windowManager.removeView(highlightView)
        serviceStarted = false

        hideForegroundNotification()

        return true
    }

    private var entryPoint: EntryPoint? = null

    private fun onScriptExit() {
        scriptCtrlBtn.post {
            scriptCtrlBtn.setImageResource(R.drawable.ic_play)
        }

        clearSupportCache()

        entryPoint = null
        scriptStarted = false

        val rec = recording
        if (rec != null) {
            // record for 2 seconds more to show things like error messages
            scriptCtrlBtn.postDelayed(2000) { rec.close() }
        }

        recording = null
    }

    private fun getEntryPoint(): EntryPoint = when (Preferences.ScriptMode) {
        ScriptModeEnum.Lottery -> AutoLottery()
        ScriptModeEnum.FriendGacha -> AutoFriendGacha()
        ScriptModeEnum.SupportImageMaker -> SupportImageMaker(::supportImgMakerCallback)
        else -> AutoBattle()
    }

    private fun supportImgMakerCallback(Id: String) {
        val i = Intent(applicationContext, SupportImageNamerActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.putExtra(SupportImageIdKey, Id)

        applicationContext.startActivity(i)
    }

    private fun startScript() {
        if (!serviceStarted || scriptStarted) {
            return
        }

        if (Preferences.RecordScreen && mediaProjection != null) {
            recording = MediaProjectionRecording(mediaProjection!!, metrics)
        }

        entryPoint = getEntryPoint().apply {
            scriptExitListener = ::onScriptExit

            scriptCtrlBtn.setImageResource(R.drawable.ic_stop)

            run()
        }

        scriptStarted = true

        showStatusNotification("Script Running")
    }

    private fun stopScript() {
        if (!scriptStarted) {
            return
        }

        entryPoint?.let {
            it.scriptExitListener = null
            it.stop()
        }

        onScriptExit()

        showStatusNotification("Ready")
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

    @SuppressLint("RtlHardcoded")
    override fun onServiceConnected() {
        Instance = this
        AutomataApi.registerPlatform(AndroidImpl(this))

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        layout = FrameLayout(this)
        layoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            @SuppressLint("RtlHardcoded")
            gravity = Gravity.LEFT or Gravity.TOP
            x = 0
            y = 0
        }

        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.script_runner, layout)

        highlightLayoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }

        scriptCtrlBtn = layout.findViewById<ImageButton>(R.id.script_toggle_btn).also {
            it.setOnClickListener {
                if (scriptStarted) {
                    stopScript()
                } else startScript()
            }

            it.setOnTouchListener(::scriptCtrlBtnOnTouch)
        }

        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        windowManager.defaultDisplay.getRealMetrics(metrics)

        // Retrieve images in Landscape
        if (metrics.heightPixels > metrics.widthPixels) {
            metrics.let {
                val temp = it.widthPixels
                it.widthPixels = it.heightPixels
                it.heightPixels = temp
            }
        }

        layoutParams.y = metrics.widthPixels

        super.onServiceConnected()
    }

    private fun getMaxBtnCoordinates(): Location {
        val rotation = windowManager.defaultDisplay.rotation
        val rotate = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180

        val x = (if (rotate) {
            metrics.heightPixels
        } else metrics.widthPixels) - layout.measuredWidth
        val y = (if (rotate) {
            metrics.widthPixels
        } else metrics.heightPixels) - layout.measuredHeight

        return Location(x, y)
    }

    private val dragStopwatch = Stopwatch()
    private var dX = 0f
    private var dY = 0f
    private var lastAction = 0

    private fun scriptCtrlBtnOnTouch(_View: View, Event: MotionEvent): Boolean {
        return when (Event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val (maxX, maxY) = getMaxBtnCoordinates()
                dX = layoutParams.x.coerceIn(0, maxX) - Event.rawX
                dY = layoutParams.y.coerceIn(0, maxY) - Event.rawY
                lastAction = MotionEvent.ACTION_DOWN
                dragStopwatch.start()

                false
            }
            MotionEvent.ACTION_MOVE -> {
                val newX = Event.rawX + dX
                val newY = Event.rawY + dY

                if (dragStopwatch.elapsedMs > ViewConfiguration.getLongPressTimeout()) {
                    val (mX, mY) = getMaxBtnCoordinates()
                    layoutParams.x = newX.roundToInt().coerceIn(0, mX)
                    layoutParams.y = newY.roundToInt().coerceIn(0, mY)

                    windowManager.updateViewLayout(layout, layoutParams)

                    lastAction = MotionEvent.ACTION_MOVE
                }

                true
            }
            MotionEvent.ACTION_UP -> lastAction == MotionEvent.ACTION_MOVE
            else -> false
        }
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    private val channelId = "fategrandautomata-notifications"
    private var channelCreated = false

    private fun createNotificationChannel() {
        if (channelCreated) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelId,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelId
            }

            val notifyManager = NotificationManagerCompat.from(this)

            notifyManager.createNotificationChannel(channel)
        }

        channelCreated = true
    }

    fun hideForegroundNotification() = stopForeground(true)

    private fun startBuildNotification(): NotificationCompat.Builder {
        createNotificationChannel()

        val activityIntent = PendingIntent
            .getActivity(this, 0, Intent(this, MainActivity::class.java), 0)

        return NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Accessibility Service Running")
            .setSmallIcon(R.mipmap.notification_icon)
            .setColor(getColor(R.color.colorBusterWeak))
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(activityIntent)
    }

    private val foregroundNotificationId = 1

    fun showStatusNotification(Message: String) {
        val builder = startBuildNotification()
            .setContentText(Message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(Message))

        NotificationManagerCompat
            .from(this)
            .notify(foregroundNotificationId, builder.build())
    }

    fun showForegroundNotification() {
        val builder = startBuildNotification()

        startForeground(foregroundNotificationId, builder.build())
    }
}
