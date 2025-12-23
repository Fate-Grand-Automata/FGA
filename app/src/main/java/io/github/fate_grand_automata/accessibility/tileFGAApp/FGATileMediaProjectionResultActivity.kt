package io.github.fate_grand_automata.accessibility.tileFGAApp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.github.fate_grand_automata.runner.ScriptRunnerService

class FGATileMediaProjectionResultActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionIntent = intent.getParcelableExtra<Intent>("PERMISSION_INTENT")
        if (permissionIntent != null) {
            startActivityForResult(permissionIntent, REQUEST_CODE_SCREEN_CAPTURE)
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                // Permission granted - store the result and start service
                ScriptRunnerService.mediaProjectionToken = data
                Log.i("MediaProjectionResult", "Permission granted, starting service")

                // Start the service immediately
                try {
                    ScriptRunnerService.Companion.startService(this)
                } catch (e: Exception) {
                    Log.e("MediaProjectionResult", "Failed to start service", e)
                }
            } else {
                // Permission denied - do nothing as requested
                Log.i("MediaProjectionResult", "Permission denied - do nothing")
            }
        }

        finish() // Always close this activity
    }

    companion object {
        private const val REQUEST_CODE_SCREEN_CAPTURE = 1001
    }
}