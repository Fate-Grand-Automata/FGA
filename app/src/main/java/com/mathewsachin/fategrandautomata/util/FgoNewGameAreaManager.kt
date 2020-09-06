package com.mathewsachin.fategrandautomata.util

import android.view.WindowManager
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.FgoGameAreaManager
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.*
import com.mathewsachin.libautomata.extensions.IDurationExtensions
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import kotlin.time.seconds

class FgoNewGameAreaManager @Inject constructor(
    val screenshotService: IScreenshotService,
    val windowManager: WindowManager,
    val prefs: IPreferences,
    val durationExtensions: IDurationExtensions,
    val exitManager: ExitManager
) : GameAreaManager, IDurationExtensions by durationExtensions {
    private fun getRegion() = screenshotService.takeScreenshot().let { img ->
        if (img is DroidCvPattern) {
            val threshold = MatOfByte()
            Imgproc.threshold(img.Mat, threshold, 1.0, 255.0, Imgproc.THRESH_BINARY)

            // OPEN removes small objects
            val kernel = Mat.ones(30, 30, CvType.CV_8UC1)
            Imgproc.morphologyEx(threshold, threshold, Imgproc.MORPH_OPEN, kernel)

            val thres = 0.7
            val w = threshold.width()
            val h = threshold.height()

            fun countHorizontal(cols: Iterable<Int>) = cols
                .asSequence()
                .map { col ->
                    (0 until h)
                        .map { row -> threshold.get(row, col)[0] }
                        .count { pixel -> pixel == 0.0 } / h.toDouble()
                }
                .takeWhile { it >= thres }
                .count()

            val left = countHorizontal(0 until w)
            val right = w - countHorizontal((w - 1) downTo 0)

            fun countVertical(rows: Iterable<Int>) = rows
                .asSequence()
                .map { row ->
                    (0 until w)
                        .map { col -> threshold.get(row, col)[0] }
                        .count { pixel -> pixel == 0.0 } / w.toDouble()
                }
                .takeWhile { it >= thres }
                .count()

            val top = countVertical(0 until h)
            val bottom = h - countVertical((h - 1) downTo 0)

            if (left >= right || top >= bottom) {
                throw Exception("Couldn't detect region")
            }

            val sizeThreshold = 0.65
            val newWidth = right - left
            val newHeight = bottom - top

            if ((newWidth * newHeight) / (w * h).toDouble() < sizeThreshold) {
                throw Exception("Size threshold not met")
            }

            Region(left, top, newWidth, newHeight)
        } else Region(Location(), img.Size)
    }

    private var rotation = 0
    private var currentGameAreaManager: GameAreaManager? = null

    private fun getRotation() = windowManager.defaultDisplay.rotation

    private fun <T> retryOnFailure(block: () -> T): T {
        while (true) {
            exitManager.checkExitRequested()

            try {
                return block()
            } catch (e: Exception) {
                // Swallow exceptions
            }
        }
    }

    private fun newGameAreaManager(): GameAreaManager {
        // In-case width or height gets detected as zero.
        val region = retryOnFailure { getRegion() }
        rotation = getRotation()

        prefs.ignoreNotchCalculation = region.X == 0

        return FgoGameAreaManager(
            { region },
            Game.scriptSize,
            Game.imageSize
        ).also { m -> currentGameAreaManager = m }
    }

    private fun getGameAreaManager(): GameAreaManager {
        return currentGameAreaManager.let {
            when {
                it == null -> newGameAreaManager()
                getRotation() != rotation -> {
                    // Allow rotation animations to finish
                    0.5.seconds.wait()
                    newGameAreaManager()
                }
                else -> it
            }
        }
    }

    override val compareDimension get() = getGameAreaManager().compareDimension
    override val scriptDimension get() = getGameAreaManager().scriptDimension
    override val gameArea get() = getGameAreaManager().gameArea
}