package io.github.fate_grand_automata.imaging

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import io.github.fate_grand_automata.util.StorageProvider
import io.github.lib_automata.ColorManager
import io.github.lib_automata.Pattern
import io.github.lib_automata.ScreenshotService
import io.github.lib_automata.Size
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
    private val storageProvider: StorageProvider,
    private val colorManager: ColorManager
) : ScreenshotService {
    private val bufferMat = Mat()
    private val grayscaleMat = Mat()
    private val grayscalePattern = DroidCvPattern(grayscaleMat, ownsMat = false)
    private val colorMat = Mat()
    private val colorPattern = DroidCvPattern(colorMat, ownsMat = false)

    @SuppressLint("WrongConstant")
    private val imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, PixelFormat.RGBA_8888, 2)
    private val virtualDisplay = mediaProjection.apply {
        this.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                close()
            }
        }, null)
    }.createVirtualDisplay(
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

        mediaProjection.stop()
    }

    override fun startRecording() =
        MediaProjectionRecording(mediaProjection, imageSize, screenDensity, storageProvider)
}