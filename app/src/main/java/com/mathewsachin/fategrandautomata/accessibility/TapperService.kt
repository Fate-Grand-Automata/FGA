package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import timber.log.Timber

class TapperService : AccessibilityService() {
    companion object {
        private val mServiceStarted = mutableStateOf(false)
        val serviceStarted: State<Boolean> = mServiceStarted

        var instance: TapperService? = null
            private set(value) {
                field = value
                mServiceStarted.value = value != null
            }
    }

    override fun onServiceConnected() {
        Timber.i("Accessibility Service bound to system")

        // We only want events from FGO
        serviceInfo = serviceInfo.apply {
            packageNames = GameServerEnum
                .values()
                .flatMap { it.packageNames.toList() }
                .toTypedArray()
        }

        instance = this

        super.onServiceConnected()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.i("Accessibility Service unbind")
        Toast.makeText(this, "FGA Accessibility stopped", Toast.LENGTH_SHORT).show()
        instance = null

        return super.onUnbind(intent)
    }

    var detectedFgoServer = GameServerEnum.En
        private set

    /**
     * This method is called on any subscribed [AccessibilityEvent] in tapper_service.xml.
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
                    Timber.d("Detected FGO: $server")

                    detectedFgoServer = server
                }
            }
            else -> {}
        }
    }

    override fun onInterrupt() {}
}