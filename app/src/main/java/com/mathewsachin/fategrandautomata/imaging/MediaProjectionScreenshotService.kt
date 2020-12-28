package com.mathewsachin.fategrandautomata.imaging

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.libautomata.IColorScreenshotProvider
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IScreenshotService
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * This class is responsible for creating screenshots using [MediaProjection].
 */
class MediaProjectionScreenshotService(
    private val MediaProjection: MediaProjection,
    private val DisplayMetrics: DisplayMetrics,
    private val storageProvider: StorageProvider,
) : IScreenshotService, IColorScreenshotProvider {
    private val colorCorrectedMat = Mat()

    private val pattern = DroidCvPattern(colorCorrectedMat, false)

    val imageReader: ImageReader
    val virtualDisplay: VirtualDisplay

    init {
        val screenDensity = DisplayMetrics.densityDpi
        val screenWidth = DisplayMetrics.widthPixels
        val screenHeight = DisplayMetrics.heightPixels

        @SuppressLint("WrongConstant")
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)

        virtualDisplay = MediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth, screenHeight, screenDensity,
            0, imageReader.surface, null, null
        )
    }

    override fun takeScreenshot(): IPattern {
        imageReader.acquireLatestImage()?.use { img ->
            img.toMat().use {
                Imgproc.cvtColor(it, colorCorrectedMat, Imgproc.COLOR_BGRA2GRAY)
            }
        }

        return pattern
    }

    private fun Image.toMat(): Mat {
        val plane = planes[0]
        val buffer = plane.buffer

        val rowStride = plane.rowStride.toLong()

        // Buffer memory isn't copied by OpenCV
        return Mat(height, width, CvType.CV_8UC4, buffer, rowStride)
    }

    override fun takeColorScreenshot(): IPattern =
        imageReader.acquireLatestImage()?.use { img ->
            img.toMat().use {
                val mat = Mat()
                Imgproc.cvtColor(it, mat, Imgproc.COLOR_RGBA2BGR)

                DroidCvPattern(mat)
            }
        } ?: pattern.copy()

    override fun close() {
        colorCorrectedMat.release()

        pattern.close()

        virtualDisplay.release()

        imageReader.close()

        MediaProjection.stop()
    }

    override fun startRecording() =
        MediaProjectionRecording(MediaProjection, DisplayMetrics, storageProvider)
}