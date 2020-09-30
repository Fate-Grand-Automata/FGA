package com.mathewsachin.fategrandautomata.accessibility

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.milliseconds

@ServiceScoped
class ScriptRunnerNotification @Inject constructor(
    val service: Service,
    val prefs: IPreferences
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

    fun hide() = service.stopForeground(true)

    private fun startBuildNotification(): NotificationCompat.Builder {
        val activityIntent = PendingIntent
            .getActivity(service, 0, Intent(service, MainActivity::class.java), 0)

        val stopIntent = PendingIntent.getBroadcast(
            service,
            1,
            Intent(service, NotificationReceiver::class.java).apply {
                putExtra(keyAction, actionStop)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopAction = NotificationCompat.Action.Builder(
            R.drawable.ic_close,
            service.getString(R.string.notification_stop),
            stopIntent
        ).build()

        val scriptIntent = PendingIntent.getBroadcast(
            service,
            2,
            Intent(service, NotificationReceiver::class.java).apply {
                putExtra(keyAction, actionScript)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val chooseScriptAction = NotificationCompat.Action.Builder(
            R.drawable.ic_script,
            service.getString(R.string.p_script_mode),
            scriptIntent
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
            .addAction(chooseScriptAction)
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
            .setDefaults(Notification.DEFAULT_SOUND)
            .setTimeoutAfter(10_000) // 10s
            .build()

        NotificationManagerCompat.from(service)
            .notify(Ids.messageNotification, notification)

        vibrate(100.milliseconds)
    }

    private fun vibrate(Duration: Duration) {
        val v = service.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(
                    Duration.toLongMilliseconds(),
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            v.vibrate(Duration.toLongMilliseconds())
        }
    }

    fun hideMessage() {
        NotificationManagerCompat.from(service)
            .cancel(Ids.messageNotification)
    }

    companion object {
        const val actionStop = "ACTION_STOP"
        const val actionScript = "ACTION_SCRIPT"
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
                actionStop -> ScriptRunnerService.Instance?.stop()
                actionScript -> chooseScript()
            }
        }

        fun chooseScript() {
            showOverlayDialog(context) {
                setTitle(R.string.p_script_mode)
                    .setSingleChoiceItems(R.array.script_mode_labels, prefs.scriptMode.ordinal) { dialog, which ->
                        prefs.scriptMode = ScriptModeEnum.values()[which]

                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
            }

            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
        }
    }
}