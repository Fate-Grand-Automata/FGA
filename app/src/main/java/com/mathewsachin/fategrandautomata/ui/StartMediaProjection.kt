package com.mathewsachin.fategrandautomata.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.result.contract.ActivityResultContract

class StartMediaProjection : ActivityResultContract<Unit, Intent?>() {
    override fun createIntent(context: Context, input: Unit?): Intent {
        val mediaProjectionManager = context.getSystemService(MediaProjectionManager::class.java)

        return mediaProjectionManager.createScreenCaptureIntent()
    }

    override fun parseResult(resultCode: Int, result: Intent?) =
        if (resultCode != Activity.RESULT_OK)
            null
        else result
}