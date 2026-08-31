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
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.main.MainActivity
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ServiceScoped
class ScriptRunnerNotification @Inject constructor(
    private val service: Service,
    private val vibrator: Vibrator
) {

    private object Channels {
        const val service = "service"
        const val old = "fategrandautomata-notifications"
        const val message = "message"
    }

    private object Ids {
        const val foregroundNotification = 1

        const val messageNotification = 2
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyManager = NotificationManagerCompat.from(service)

            NotificationChannel(
                Channels.service,
                "Service Running",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification that the service is running in the background"

                setShowBadge(false)
                notifyManager.createNotificationChannel(this)
            }

            NotificationChannel(
                Channels.message,
                "Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Plays sound on Script exit and other events"

                setShowBadge(false)
                notifyManager.createNotificationChannel(this)
            }

            try {
                // Delete the old channel
                notifyManager.deleteNotificationChannel(Channels.old)
            } catch (e: Exception) {
            }
        }
    }

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

        return NotificationCompat.Builder(service, Channels.service)
            .setOngoing(true)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText(service.getString(R.string.overlay_notification_text))
            // show full message on expand
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(service.getString(R.string.overlay_notification_text)))
            .setSmallIcon(R.mipmap.notification_icon)
            .setColor(service.getColor(R.color.colorBusterWeak))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(activityIntent)
            .addAction(stopAction)
    }

    /**
     * Promotes the service to the foreground.
     *
     * [withMediaProjection] may only be true while we actually hold MediaProjection consent.
     * Android 14+ rejects the mediaProjection foreground service type with a SecurityException
     * unless the `android:project_media` app-op is currently granted, and that exception is
     * fatal because it escapes Service.onCreate().
     *
     * Can be called again to add the mediaProjection type once consent arrives.
     *
     * @return false if the system refused to start the foreground service
     */
    fun show(withMediaProjection: Boolean): Boolean {
        val builder = startBuildNotification()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                var foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                if (withMediaProjection) {
                    foregroundServiceType =
                        foregroundServiceType.or(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                }

                service.startForeground(
                    Ids.foregroundNotification,
                    builder.build(),
                    foregroundServiceType
                )
            } else {
                service.startForeground(
                    Ids.foregroundNotification,
                    builder.build()
                )
            }
        } catch (e: RuntimeException) {
            // SecurityException, ForegroundServiceStartNotAllowedException,
            // ForegroundServiceTypeException, ... - the caller has to shut the service down,
            // but crashing the app here helps nobody.
            Timber.e(e, "Couldn't start the foreground service")
            return false
        }

        return true
    }

    fun message(msg: String) {
        val notification = NotificationCompat.Builder(service, Channels.message)
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
            NotificationManagerCompat.from(service).notify(Ids.messageNotification, notification)
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
            .cancel(Ids.messageNotification)
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