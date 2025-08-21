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

    /* Called when the user taps on your tile in an active or inactive state.
    *
    * Android 13 and below:
    * - Will not prompt MediaProjectionToken if it's not expired
    *
    * Android 14 and above:
    * - Will prompt everytime because the token is single used, change of behaviour since Android 14
    * source:
    * 1) https://developer.android.com/about/versions/14/behavior-changes-14
    * 2) https://developer.android.com/media/grow/media-projection
    */
    override fun onClick() {
        super.onClick()

        val isServiceRunning = isServiceRunning()

        if (isServiceRunning) {
            // Stop the service
            ScriptRunnerService.stopService(this)
        } else {
            if (!hasScreenRecordingPermission()) {
                requestScreenRecordingPermission()
                return
            }

            try {
                ScriptRunnerService.startService(this)
            } catch (e: Exception) {
                Log.e("FGATileService", "Direct service start failed, opening FGA App instead.", e)
                openFGAAppWithStartIntent()
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
     * Opens FGA app if fail to launch service initially onClick().
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
        } else {
            //Android 13 and below fallback
            startActivity(intent)
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
        return ScriptRunnerService.serviceStarted.value
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
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