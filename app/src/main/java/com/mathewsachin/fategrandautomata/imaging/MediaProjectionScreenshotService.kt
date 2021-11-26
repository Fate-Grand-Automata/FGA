package com.mathewsachin.fategrandautomata.imaging

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.libautomata.ColorManager
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
    private val colorManager: ColorManager
) : IScreenshotService {
    private val bufferMat = Mat()
    private val grayscaleMat = Mat()
    private val grayscalePattern = DroidCvPattern(grayscaleMat, ownsMat = false)
    private val colorMat = Mat()
    private val colorPattern = DroidCvPattern(colorMat, ownsMat = false)

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
        screenshotIntoBuffer()

        return if (colorManager.isColor) {
            Imgproc.cvtColor(bufferMat, colorMat, Imgproc.COLOR_RGBA2BGR)

            colorPattern
        } else {
            Imgproc.cvtColor(bufferMat, grayscaleMat, Imgproc.COLOR_RGBA2GRAY)

            grayscalePattern
        }
    }

    private fun screenshotIntoBuffer() {
        imageReader.acquireLatestImage()?.use {
            val plane = it.planes[0]
            val buffer = plane.buffer

            val rowStride = plane.rowStride.toLong()

            // Buffer memory isn't copied by OpenCV
            Mat(it.height, it.width, CvType.CV_8UC4, buffer, rowStride)
                .use { tempMat ->
                    tempMat.copyTo(bufferMat)
                }
        }
    }

    override fun close() {
        bufferMat.release()
        grayscaleMat.release()
        grayscalePattern.close()
        colorMat.release()
        colorPattern.close()

        virtualDisplay.release()

        imageReader.close()

        MediaProjection.stop()
    }

    override fun startRecording() =
        MediaProjectionRecording(MediaProjection, DisplayMetrics, storageProvider)
}