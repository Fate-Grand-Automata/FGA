package com.mathewsachin.fategrandautomata.root

import android.os.Build
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.readIntLE
import com.mathewsachin.libautomata.ColorManager
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IScreenshotService
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import timber.log.debug
import timber.log.error
import java.io.DataInputStream

class RootScreenshotService(
    private val SuperUser: SuperUser,
    val storageProvider: StorageProvider,
    private val colorManager: ColorManager
) : IScreenshotService {
    private var reader: DataInputStream = SuperUser.inStream
    private var buffer: ByteArray? = null

    private var bufferMat: Mat? = null
    private val grayscaleMat = Mat()
    private val grayscalePattern = DroidCvPattern(grayscaleMat, ownsMat = false)
    private val colorMat = Mat()
    private val colorPattern = DroidCvPattern(colorMat, ownsMat = false)

    private fun screenshotIntoBuffer() {
        SuperUser.writeLine("/system/bin/screencap")

        val w = reader.readIntLE()
        val h = reader.readIntLE()
        val format = reader.readIntLE()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reader.readIntLE()
        }

        if (buffer == null) {
            // If format is not RGBA
            if (format != 1) {
                Timber.error { "Unexpected raw image format: $format" }
            }

            Timber.debug { "${w}x${h} format=$format" }

            buffer = ByteArray(w * h * 4)
            bufferMat = Mat(h, w, CvType.CV_8UC4)
        }

        // "readFully" will wait for the entire data (b.size) to be available in the input stream,
        // however long it takes (in actuality, it just takes a few milliseconds).
        buffer?.let { b -> reader.readFully(b, 0, b.size) }
        bufferMat?.put(0, 0, buffer)
    }

    override fun takeScreenshot(): IPattern {
        screenshotIntoBuffer()

        return if (colorManager.isColor) {
            Imgproc.cvtColor(bufferMat, colorMat, Imgproc.COLOR_RGBA2BGR)

            colorPattern
        }
        else {
            Imgproc.cvtColor(bufferMat, grayscaleMat, Imgproc.COLOR_RGBA2GRAY)

            grayscalePattern
        }
    }

    override fun close() {
        bufferMat?.release()
        grayscaleMat.release()
        colorMat.release()

        try {
            SuperUser.close()
        } catch (e: Exception) {
            Timber.error(e) { "Error closing super user" }
        }

        grayscalePattern.close()
        colorPattern.close()

        buffer = null
    }

    override fun startRecording(): AutoCloseable? = null
}