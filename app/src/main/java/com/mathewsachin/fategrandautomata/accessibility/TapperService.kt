package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import timber.log.Timber
import timber.log.debug
import timber.log.info

class TapperService: AccessibilityService() {
    companion object {
        var instance: TapperService? = null
            private set

        val isRunning get() = instance != null
    }

    override fun onServiceConnected() {
        Timber.info { "Accessibility Service bound to system" }

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
        Timber.info { "Accessibility Service unbind" }
        instance = null

        return super.onUnbind(intent)
    }

    var detectedFgoServer = GameServerEnum.En
        private set

    /**
     * This method is called on any subscribed [AccessibilityEvent] in script_runner_service.xml.
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
                    Timber.debug { "Detected FGO: $server" }

                    detectedFgoServer = server
                }
            }
        }
    }

    override fun onInterrupt() {}
}