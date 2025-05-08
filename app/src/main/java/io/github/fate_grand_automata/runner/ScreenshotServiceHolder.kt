package io.github.fate_grand_automata.runner

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.imaging.MediaProjectionScreenshotService
import io.github.fate_grand_automata.root.RootScreenshotService
import io.github.fate_grand_automata.root.SuperUser
import io.github.fate_grand_automata.scripts.FgoGameAreaManager
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.scripts.prefs.wantsMediaProjectionToken
import io.github.fate_grand_automata.util.DisplayHelper
import io.github.fate_grand_automata.util.StorageProvider
import io.github.fate_grand_automata.util.makeLandscape
import io.github.lib_automata.ColorManager
import io.github.lib_automata.Location
import io.github.lib_automata.PlatformImpl
import io.github.lib_automata.RealScale
import io.github.lib_automata.ResizedScreenshotProvider
import io.github.lib_automata.ScreenshotService
import io.github.lib_automata.Size
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@ServiceScoped
class ScreenshotServiceHolder @Inject constructor(
    private val prefs: IPreferences,
    private val storageProvider: StorageProvider,
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
                // Cloning the Intent allows reuse.
                // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
                val token = ScriptRunnerService.mediaProjectionToken?.clone() as Intent

                val mediaProjection =
                    mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, token)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // not allowed to reuse tokens on Android 14
                    ScriptRunnerService.mediaProjectionToken = null
                }

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