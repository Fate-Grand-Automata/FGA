package com.mathewsachin.fategrandautomata.runner

import android.media.projection.MediaProjectionManager
import com.mathewsachin.fategrandautomata.imaging.MediaProjectionScreenshotService
import com.mathewsachin.fategrandautomata.root.RootScreenshotService
import com.mathewsachin.fategrandautomata.root.SuperUser
import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.wantsMediaProjectionToken
import com.mathewsachin.fategrandautomata.util.DisplayHelper
import com.mathewsachin.fategrandautomata.util.makeLandscape
import com.mathewsachin.libautomata.*
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@ServiceScoped
class ScreenshotServiceHolder @Inject constructor(
    private val prefs: IPreferences,
    private val display: DisplayHelper,
    private val colorManager: ColorManager,
    private val mediaProjectionManager: MediaProjectionManager,
    private val platformImpl: PlatformImpl
) : AutoCloseable {
    var screenshotService: ScreenshotService? = null
        private set

    fun prepareScreenshotService() {
        val landscapeMetrics = display.metrics.makeLandscape()
        val size = Size(landscapeMetrics.widthPixels, landscapeMetrics.heightPixels)
        val scale = RealScale(
            gameAreaManager = FgoGameAreaManager(
                size,
                offset = { Location() }
            )
        ).screenToImage

        screenshotService = try {
            if (prefs.wantsMediaProjectionToken) {
                val scaledSize = size * (scale ?: 1.0)
                val scaledDensity = (landscapeMetrics.densityDpi / (scale ?: 1.0)).roundToInt()

                MediaProjectionScreenshotService(
                    mediaProjectionManager,
                    ScriptRunnerService.mediaProjectionToken!!,
                    scaledSize,
                    scaledDensity,
                    colorManager
                )
            } else {
                val rootSS = RootScreenshotService(
                    SuperUser(),
                    colorManager
                )

                if (scale != null) {
                    ResizedScreenshotProvider(
                        rootSS,
                        scale,
                        platformImpl
                    )
                } else rootSS
            }
        } catch (e: Exception) {
            Timber.e(e, "Error preparing screenshot service")
            null
        }
    }

    override fun close() {
        screenshotService?.close()
        screenshotService = null
    }
}