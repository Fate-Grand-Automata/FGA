package io.github.fate_grand_automata.util

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.app.NotificationChannelGroupCompat
import io.github.fate_grand_automata.R


/**
 * Helper method to build a notification channel group.
 *
 * @param channelId the channel id.
 * @param block the function that will execute inside the builder.
 * @return a notification channel group to be displayed or updated.
 */
fun buildNotificationChannelGroup(
    channelId: String,
    block: (NotificationChannelGroupCompat.Builder.() -> Unit),
): NotificationChannelGroupCompat {
    val builder = NotificationChannelGroupCompat.Builder(channelId)
    builder.block()
    return builder.build()
}

/**
 * Helper method to build a notification channel.
 *
 * @param channelId the channel id.
 * @param channelImportance the channel importance.
 * @param block the function that will execute inside the builder.
 * @return a notification channel to be displayed or updated.
 */
fun buildNotificationChannel(
    channelId: String,
    channelImportance: Int,
    block: (NotificationChannelCompat.Builder.() -> Unit),
): NotificationChannelCompat {
    val builder = NotificationChannelCompat.Builder(channelId, channelImportance)
    builder.block()
    return builder.build()
}

object Notifications {

    /**
     * Notification channel for the script service
     * @see io.github.fate_grand_automata.runner.ScriptRunnerNotification
     */
    const val SCRIPT_SERVICE_CHANNEL = "Service"
    const val SCRIPT_SERVICE_ID = 1

    /**
     * Notification channel for the script messages
     * @see io.github.fate_grand_automata.runner.ScriptRunnerNotification
     */
    const val SCRIPT_MESSAGE_CHANNEL = "Message"
    const val SCRIPT_MESSAGE_ID = 2

    private val deprecatedChannels = listOf("fategrandautomata-notifications")


    /**
     * Creates the notification channels introduced in Android Oreo.
     * This won't do anything on Android versions that don't support notification channels.
     *
     * @param context The application context.
     */
    fun createChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Delete the old channels
        deprecatedChannels.forEach(notificationManager::deleteNotificationChannel)

        notificationManager.createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(SCRIPT_SERVICE_CHANNEL, IMPORTANCE_LOW) {
                    setName(context.getString(R.string.notification_script_service))
                    setDescription(context.getString(R.string.notification_script_service_desc))
                    setShowBadge(false)
                },
                buildNotificationChannel(SCRIPT_MESSAGE_CHANNEL, IMPORTANCE_DEFAULT) {
                    setName(context.getString(R.string.notification_script_message))
                    setDescription(context.getString(R.string.notification_script_message_desc))
                    setShowBadge(false)
                }
            )
        )
    }
}