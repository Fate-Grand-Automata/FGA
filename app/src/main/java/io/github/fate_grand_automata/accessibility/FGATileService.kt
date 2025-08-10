package io.github.fate_grand_automata.accessibility

import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.fate_grand_automata.runner.ScriptRunnerService

class FGATileService: TileService() {

    private var updateHandler: Handler? = null
    private var updateRunnable: Runnable? = null
    private var lastServiceState: Boolean = false
    private var lastAccessibilityState: Boolean = false

    // Called when the user adds your tile.
    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()

        // Initialize state tracking
        lastServiceState = isServiceRunning()
        lastAccessibilityState = isAccessibilityServiceEnabled()

        // Set up periodic checking for state changes
        updateHandler = Handler(Looper.getMainLooper())
        updateRunnable = object : Runnable {
            override fun run() {
                val currentServiceState = isServiceRunning()
                val currentAccessibilityState = isAccessibilityServiceEnabled()

                // Only update tile if states have changed
                if (currentServiceState != lastServiceState ||
                    currentAccessibilityState != lastAccessibilityState) {

                    lastServiceState = currentServiceState
                    lastAccessibilityState = currentAccessibilityState
                    updateTile()
                }

                // Schedule next check
                updateHandler?.postDelayed(this, 100) // Check every 100ms
            }
        }

        updateTile()
        updateHandler?.post(updateRunnable!!)
    }

    // Called when the user removes your tile.
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    // Called when your app can no longer update your tile.
    override fun onStopListening() {
        super.onStopListening()

        // Stop periodic updates
        updateRunnable?.let { runnable ->
            updateHandler?.removeCallbacks(runnable)
        }
        updateHandler = null
        updateRunnable = null
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()

        val isServiceRunning = isServiceRunning()

        if (isServiceRunning) {
            // Stop the service
            try {
                ScriptRunnerService.stopService(this)
            } catch (e: Exception) {
                android.util.Log.e("FGATileService", "Failed to stop service", e)
            }
        } else {
            // Check permissions before starting service
            if (!isAccessibilityServiceEnabled() || !canDrawOverlays()) {
                // Return no notification/error code
                return
            }

            // Start the service - accessibility is granted
            try {
                ScriptRunnerService.startService(this)
            } catch (e: Exception) {
                android.util.Log.e("FGATileService", "Failed to start service", e)
            }
        }

        updateTile()
    }

    private fun updateTile() {
        qsTile?.let { tile ->
            val isServiceRunning = isServiceRunning()
            val hasAccessibility = isAccessibilityServiceEnabled()
            val hasOverlay = canDrawOverlays()
            val hasAllPermissions = hasAccessibility && hasOverlay

            // Check service is running first, before changing label
            if (isServiceRunning) {
                tile.state = Tile.STATE_ACTIVE

                // To check if device OS can put subtitle under label, if not change label entirely
                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {
                    tile.subtitle = "Stop"
                } else {
                    tile.label = "FGA Service - STOP"
                }
            } else {
                // Show different states based on both permissions
                if (hasAllPermissions) {
                    tile.state = Tile.STATE_INACTIVE
                    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {
                        tile.subtitle = "Start"
                    } else {
                        tile.label = "FGA Service - START"
                    }
                } else {
                    tile.state = Tile.STATE_UNAVAILABLE
                    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {
                        tile.subtitle = "Permissions Required"
                    } else {
                        tile.label = "FGA - Permissions Required"
                    }
                }
            }

            // Update the tile to reflect changes
            tile.updateTile()
        }
    }

    private fun isServiceRunning(): Boolean {
        // Use the same method as the main app for consistency
        return ScriptRunnerService.serviceStarted.value
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        // Check if TapperService is enabled and running
        return TapperService.serviceStarted.value
    }

    private fun canDrawOverlays(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Permission not required for API < 23
        }
    }
}