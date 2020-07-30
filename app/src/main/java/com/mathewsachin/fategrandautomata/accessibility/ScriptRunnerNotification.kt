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
import com.mathewsachin.fategrandautomata.dagger.service.ServiceScope
import com.mathewsachin.fategrandautomata.ui.MainActivity
import javax.inject.Inject

@ServiceScope
class ScriptRunnerNotification @Inject constructor(val service: Service) {

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

            val notifyManager = NotificationManagerCompat.from(service)

            notifyManager.createNotificationChannel(channel)
        }

        channelCreated = true
    }

    fun hide() = service.stopForeground(true)

    private fun startBuildNotification(): NotificationCompat.Builder {
        createNotificationChannel()

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
            "Stop",
            stopIntent
        ).build()

        return NotificationCompat.Builder(service, channelId)
            .setOngoing(true)
            .setContentTitle(service.getString(R.string.app_name))
            .setContentText("Accessibility Service Running")
            .setSmallIcon(R.mipmap.notification_icon)
            .setColor(service.getColor(R.color.colorBusterWeak))
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(activityIntent)
            .addAction(stopAction)
    }

    private val foregroundNotificationId = 1

    fun show() {
        val builder = startBuildNotification()

        service.startForeground(foregroundNotificationId, builder.build())
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