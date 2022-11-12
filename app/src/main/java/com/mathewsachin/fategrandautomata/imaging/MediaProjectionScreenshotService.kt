package com.mathewsachin.fategrandautomata.imaging

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import com.mathewsachin.libautomata.ColorManager
import com.mathewsachin.libautomata.Pattern
import com.mathewsachin.libautomata.ScreenshotService
import com.mathewsachin.libautomata.Size
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * This class is responsible for creating screenshots using [mediaProjection].
 */
class MediaProjectionScreenshotService(
    private val mediaProjection: MediaProjection,
    private val imageSize: Size,
    private val screenDensity: Int,
    private val colorManager: ColorManager
) : ScreenshotService {
    private val bufferMat = Mat()
    private val grayscaleMat = Mat()
    private val grayscalePattern = DroidCvPattern(grayscaleMat, ownsMat = false)
    private val colorMat = Mat()
    private val colorPattern = DroidCvPattern(colorMat, ownsMat = false)

    @SuppressLint("WrongConstant")
    private val imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, PixelFormat.RGBA_8888, 2)
    private var virtualDisplay = createVirtualDisplay()

    private fun createVirtualDisplay() = mediaProjection.createVirtualDisplay(
        "ScreenCapture",
        imageSize.width, imageSize.height, screenDensity,
        0, imageReader.surface, null, null
    )

    override fun takeScreenshot(): Pattern {
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
        var image = imageReader.acquireLatestImage()
        if (image == null) {
            // restart MediaProjection
            virtualDisplay.release()
            virtualDisplay = createVirtualDisplay()
            image = imageReader.acquireLatestImage()
        }
        image?.use {
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

        mediaProjection.stop()
    }
}