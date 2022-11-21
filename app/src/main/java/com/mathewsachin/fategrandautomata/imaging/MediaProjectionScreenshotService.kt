package com.mathewsachin.fategrandautomata.imaging

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
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
    private val mediaProjectionManager: MediaProjectionManager,
    private val mediaProjectionToken: Intent,
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
    private lateinit var mediaProjection: MediaProjection
    private lateinit var virtualDisplay: VirtualDisplay

    init {
        startMediaProjection()
    }

    private fun startMediaProjection() {
        // Cloning the Intent allows reuse.
        // Otherwise, the Intent gets consumed and MediaProjection cannot be started multiple times.
        mediaProjection =
            mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionToken.clone() as Intent)
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            imageSize.width, imageSize.height, screenDensity,
            0, imageReader.surface, null, null
        )
    }

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
            mediaProjection.stop()

            startMediaProjection()
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