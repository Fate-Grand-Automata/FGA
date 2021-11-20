package com.mathewsachin.fategrandautomata.runner

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.wantsMediaProjectionToken
import com.mathewsachin.fategrandautomata.util.DisplayHelper
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.makeLandscape
import com.mathewsachin.libautomata.ColorManager
import com.mathewsachin.libautomata.IScreenshotService
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber
import timber.log.error
import javax.inject.Inject
import javax.inject.Provider

@ServiceScoped
class ScreenshotServiceHolder @Inject constructor(
    private val prefs: IPreferences,
    private val storageProvider: StorageProvider,
    private val display: DisplayHelper,
    private val colorManager: ColorManager,
    private val rootScreenshotServiceProvider: Provider<RootScreenshotService>,
    private val mediaProjectionManager: MediaProjectionManager
) : AutoCloseable {
    val screenshotService: IScreenshotService? by lazy {
        try {
            if (prefs.wantsMediaProjectionToken) {
                // Cloning the Intent allows reuse.
                // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
                val token = ScriptRunnerService.mediaProjectionToken?.clone() as? Intent
                    ?: throw IllegalStateException("MediaProjection token is null")

                val mediaProjection =
                    mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, token)
                MediaProjectionScreenshotService(
                    mediaProjection!!,
                    display.metrics.makeLandscape(),
                    storageProvider,
                    colorManager
                )
            } else rootScreenshotServiceProvider.get()
        } catch (e: Exception) {
            Timber.error(e) { "Error preparing screenshot service" }
            null
        }
    }

    override fun close() {
        screenshotService?.close()
    }
}