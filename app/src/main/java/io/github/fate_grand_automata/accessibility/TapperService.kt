package io.github.fate_grand_automata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.fate_grand_automata.scripts.enums.GameServer
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
            packageNames = GameServer.packageNames.keys.toTypedArray(),
            flags = AccessibilityServiceInfo.DEFAULT or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE,
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
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

    var detectedFgoServer = GameServer.default
        private set

    /**
     * This method is called on any subscribed [AccessibilityEvent] in tapper_service.xml.
     *
     * When the app in the foreground changes, this method will check if the foreground app is one
     * of the FGO APKs.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source = event?.source
        source?.let {
            source.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        }
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val foregroundAppName = event.packageName?.toString()
                    ?: return

                GameServer.fromPackageName(foregroundAppName)?.let { server ->
                    Timber.d("Detected FGO: $server")

                    detectedFgoServer = server
                }
            }

            else -> {}
        }
    }

    override fun onInterrupt() {}
}