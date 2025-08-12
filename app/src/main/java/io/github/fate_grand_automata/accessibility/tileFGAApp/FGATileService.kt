package io.github.fate_grand_automata.accessibility.tileFGAApp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import io.github.fate_grand_automata.accessibility.TapperService
import io.github.fate_grand_automata.runner.ScriptRunnerService
import io.github.fate_grand_automata.ui.main.MainActivity

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
            ScriptRunnerService.stopService(this)
        } else {

            if(!hasScreenRecordingPermission()) {
                requestScreenRecordingPermission()
                return
            }

            // For Android 14+ (API 34+), always use activity to start service
            // For older versions, try direct start first, fallback to activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: Always open activity to avoid ForegroundServiceStartNotAllowedException
                openFGAAppWithStartIntent()
            } else {
                // Pre-Android 14: Try direct start, fallback to activity if needed
                try {
                    ScriptRunnerService.startService(this)
                } catch (e: Exception) {
                    Log.e("FGATileService", "Direct service start failed, opening activity", e)
                    openFGAAppWithStartIntent()
                }
            }
        }

        updateTile()
    }

    private fun requestScreenRecordingPermission() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()

        // This replicates the startMediaProjection.launch() functionality
        val resultIntent = Intent(this, FGATileMediaProjectionResultActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("PERMISSION_INTENT", permissionIntent)
        }

        startActivity(resultIntent)
    }

    /**
     * Opens FGA app with intent to start service from there - this is strictly for Android 14+ behaviour
     *
     * This is to fix crashes for Android 14 and above devices
     *
     * For Android 13 and below, it will start the service as normal
     */
    private fun openFGAAppWithStartIntent() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("START_SERVICE_FROM_TILE", true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        }
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
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    tile.subtitle = "Stop"
                } else {
                    tile.label = "FGA Service - STOP"
                }
            } else {
                // Show different states based on both permissions
                if (hasAllPermissions) {
                    tile.state = Tile.STATE_INACTIVE
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        tile.subtitle = "Start"
                    } else {
                        tile.label = "FGA Service - START"
                    }
                } else {
                    tile.state = Tile.STATE_UNAVAILABLE
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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

    private fun hasScreenRecordingPermission(): Boolean {
        return ScriptRunnerService.mediaProjectionToken != null
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true // Permission not required for API < 23
        }
    }

}