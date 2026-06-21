package io.github.fate_grand_automata.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract

class StartMediaProjection : ActivityResultContract<Unit, Intent?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val mediaProjectionManager = context.getSystemService(MediaProjectionManager::class.java)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            mediaProjectionManager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
        } else {
            mediaProjectionManager.createScreenCaptureIntent()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        if (resultCode != Activity.RESULT_OK)
            null
        else intent
}