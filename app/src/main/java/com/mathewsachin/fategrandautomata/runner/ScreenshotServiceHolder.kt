package com.mathewsachin.fategrandautomata.runner

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.fategrandautomata.scripts.prefs.wantsMediaProjectionToken
import com.mathewsachin.fategrandautomata.util.DisplayHelper
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.makeLandscape
import com.mathewsachin.libautomata.*
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

@ServiceScoped
class ScreenshotServiceHolder @Inject constructor(
    private val prefs: IPreferences,
    private val storageProvider: StorageProvider,
    private val display: DisplayHelper,
    private val colorManager: ColorManager,
    private val rootScreenshotServiceProvider: Provider<RootScreenshotService>,
    private val mediaProjectionManager: MediaProjectionManager,
    private val platformImpl: PlatformImpl
) : AutoCloseable {
    private var screenshotService: ScreenshotService? = null

    fun ensureScreenshotService(): ScreenshotService =
        screenshotService
            ?: createScreenshotService().also { screenshotService = it }

    private fun createScreenshotService(): ScreenshotService {
        val landscapeMetrics = display.metrics.makeLandscape()
        val size = Size(landscapeMetrics.widthPixels, landscapeMetrics.heightPixels)
        val scale = RealScale(
            gameAreaManager = FgoGameAreaManager(
                size,
                offset = { Location() },
                prefs.isNewUI
            )
        ).screenToImage

        return if (prefs.wantsMediaProjectionToken) {
            // Cloning the Intent allows reuse.
            // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
            val token = ScriptRunnerService.mediaProjectionToken?.clone() as? Intent
                ?: throw IllegalStateException("MediaProjection token is null")

            val mediaProjection =
                mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, token)

            val scaledSize = size * (scale ?: 1.0)
            val scaledDensity = (landscapeMetrics.densityDpi / (scale ?: 1.0)).roundToInt()

            MediaProjectionScreenshotService(
                mediaProjection,
                scaledSize,
                scaledDensity,
                storageProvider,
                colorManager
            )
        } else {
            val rootSS = rootScreenshotServiceProvider.get()

            if (scale != null) {
                ResizedScreenshotProvider(
                    rootSS,
                    scale,
                    platformImpl
                )
            } else rootSS
        }
    }

    override fun close() {
        screenshotService?.close()
    }
}