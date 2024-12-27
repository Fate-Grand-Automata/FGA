package io.github.fate_grand_automata.runner

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.util.Notifications
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.main.MainActivity
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ServiceScoped
class ScriptRunnerNotification @Inject constructor(
    private val service: Service,
    private val vibrator: Vibrator
) {

    private fun startBuildNotification(): NotificationCompat.Builder {
        val activityIntent = PendingIntent
            .getActivity(
                service,
                0,
                Intent(service, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )

        val stopIntent = PendingIntent.getBroadcast(
            service,
            1,
            Intent(service, NotificationReceiver::class.java).apply {
                putExtra(keyAction, actionStop)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_close,
            service.getString(R.string.notification_stop),
            stopIntent
        ).build()

        return NotificationCompat.Builder(service, Notifications.SCRIPT_SERVICE_CHANNEL)
            .setOngoing(true)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText(service.getString(R.string.overlay_notification_text))
            // show full message on expand
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(service.getString(R.string.overlay_notification_text)))
            .setSmallIcon(R.mipmap.notification_icon)
            .setColor(service.getColor(R.color.colorBusterWeak))
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setContentIntent(activityIntent)
            .addAction(stopAction)
    }

    fun show(useRootForScreenshots: Boolean) {
        val builder = startBuildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            var foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            if (!useRootForScreenshots) {
                foregroundServiceType = foregroundServiceType.or(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            }

            service.startForeground(
                Notifications.SCRIPT_SERVICE_ID,
                builder.build(),
                foregroundServiceType
            )
        } else {
            service.startForeground(
                Notifications.SCRIPT_SERVICE_ID,
                builder.build()
            )
        }
    }

    fun message(msg: String) {
        val notification = NotificationCompat.Builder(service, Notifications.SCRIPT_MESSAGE_CHANNEL)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText(msg)
            .setSmallIcon(R.mipmap.notification_icon)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setTimeoutAfter(10_000) // 10s
            .build()

        // Android 13+ needs a notification permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            || ContextCompat.checkSelfPermission(service, POST_NOTIFICATIONS) == PERMISSION_GRANTED
        ) {
            // only show notification if allowed
            NotificationManagerCompat.from(service).notify(Notifications.SCRIPT_MESSAGE_ID, notification)
        }

        vibrate(100.milliseconds)
    }

    private fun vibrate(duration: Duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration.inWholeMilliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration.inWholeMilliseconds)
        }
    }

    fun hideMessage() {
        NotificationManagerCompat.from(service)
            .cancel(Notifications.SCRIPT_MESSAGE_ID)
    }

    companion object {
        const val actionStop = "ACTION_STOP"
        const val keyAction = "action"
    }

    @AndroidEntryPoint
    class NotificationReceiver : BroadcastReceiver() {
        @Inject
        @ApplicationContext
        lateinit var context: Context

        @Inject
        lateinit var prefs: IPreferences

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(keyAction)) {
                actionStop -> ScriptRunnerService.stopService(context)
            }
        }
    }
}