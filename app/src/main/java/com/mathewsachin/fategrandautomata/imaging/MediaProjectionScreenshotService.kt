package com.mathewsachin.fategrandautomata.imaging

import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
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
    private val DisplayMetrics: DisplayMetrics
) : IScreenshotService {
    private val colorCorrectedMat = Mat()

    private val pattern = DroidCvPattern(colorCorrectedMat, false)

    val imageReader: ImageReader
    val virtualDisplay: VirtualDisplay

    init {
        val screenDensity = DisplayMetrics.densityDpi
        val screenWidth = DisplayMetrics.widthPixels
        val screenHeight = DisplayMetrics.heightPixels

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)

        virtualDisplay = MediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth, screenHeight, screenDensity,
            0, imageReader.surface, null, null
        )
    }

    override fun takeScreenshot(): IPattern {
        imageReader.acquireLatestImage()?.use {
            matFromImg(it)
        }

        return pattern
    }

    private fun matFromImg(Image: Image) {
        val w = Image.width
        val h = Image.height

        val plane = Image.planes[0]
        val buffer = plane.buffer

        val rowStride = plane.rowStride.toLong()

        // Buffer memory isn't copied by OpenCV
        DisposableMat(Mat(h, w, CvType.CV_8UC4, buffer, rowStride)).use {
            Imgproc.cvtColor(it.Mat, colorCorrectedMat, Imgproc.COLOR_BGRA2GRAY)
        }
    }

    override fun close() {
        colorCorrectedMat.release()

        pattern.close()

        virtualDisplay.release()

        imageReader.close()

        MediaProjection.stop()
    }

    override fun startRecording() =
        MediaProjectionRecording(MediaProjection, DisplayMetrics)
}