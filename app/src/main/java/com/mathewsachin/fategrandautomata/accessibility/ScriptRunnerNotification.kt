package com.mathewsachin.fategrandautomata.accessibility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.MainActivity
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class ScriptRunnerNotification @Inject constructor(val service: Service) {

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

    fun hide() = service.stopForeground(true)

    private fun startBuildNotification(): NotificationCompat.Builder {
        val activityIntent = PendingIntent
            .getActivity(service, 0, Intent(service, MainActivity::class.java), 0)

        val stopIntent = PendingIntent.getBroadcast(
            service,
            1,
            Intent(service, NotificationReceiver::class.java).apply {
                putExtra(NotificationReceiver.keyAction, NotificationReceiver.actionStop)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_close,
            service.getString(R.string.notification_stop),
            stopIntent
        ).build()

        return NotificationCompat.Builder(service, Channels.service)
            .setOngoing(true)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText(service.getString(R.string.notification_text))
            .setSmallIcon(R.mipmap.notification_icon)
            .setColor(service.getColor(R.color.colorBusterWeak))
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setContentIntent(activityIntent)
            .addAction(stopAction)
    }

    fun show() {
        val builder = startBuildNotification()

        service.startForeground(Ids.foregroundNotification, builder.build())
    }

    fun message(msg: String) {
        val notification = NotificationCompat.Builder(service, Channels.message)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText(msg)
            .setSmallIcon(R.mipmap.notification_icon)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setTimeoutAfter(10_000) // 10s
            .build()

        NotificationManagerCompat.from(service)
            .notify(Ids.messageNotification, notification)
    }

    class NotificationReceiver : BroadcastReceiver() {
        companion object {
            const val actionStop = "ACTION_STOP"
            const val keyAction = "action"
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra(keyAction)) {
                actionStop -> ScriptRunnerService.Instance?.stop()
            }
        }
    }
}