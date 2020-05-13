package com.mathewsachin.fategrandautomata.imaging

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.mathewsachin.fategrandautomata.core.IPattern
import com.mathewsachin.fategrandautomata.core.IScreenshotService
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class MediaProjectionScreenshotService(
    private val MediaProjection: MediaProjection,
    DisplayMetrics: DisplayMetrics
): IScreenshotService {
    private val convertedMat = Mat()
    private val colorCorrectedMat = Mat()

    private var readBitmap: Bitmap? = null
    private var cropRequired = false
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

        if (readBitmap == null) {
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * w

            cropRequired = (rowPadding / pixelStride) != 0

            readBitmap = Bitmap.createBitmap(w + rowPadding / pixelStride, h, Bitmap.Config.ARGB_8888)
        }

        readBitmap?.copyPixelsFromBuffer(buffer)

        if (cropRequired) {
            val correctedBitmap = Bitmap.createBitmap(readBitmap!!, 0, 0, w, h)
            Utils.bitmapToMat(correctedBitmap, convertedMat)
            // if a new Bitmap was created, we need to tell the Garbage Collector to delete it immediately
            correctedBitmap.recycle()
        }
        else Utils.bitmapToMat(readBitmap, convertedMat)

        Imgproc.cvtColor(convertedMat, colorCorrectedMat, Imgproc.COLOR_BGRA2GRAY)
    }

    override fun close() {
        convertedMat.release()
        colorCorrectedMat.release()

        pattern.close()

        readBitmap?.recycle()

        virtualDisplay.release()

        imageReader.close()

        MediaProjection.stop()
    }
}