package com.mathewsachin.fategrandautomata.root

import android.os.Build
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.util.readIntLE
import com.mathewsachin.libautomata.IColorScreenshotProvider
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.IScreenshotService
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import timber.log.debug
import timber.log.error
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

class RootScreenshotService(
    private val SuperUser: SuperUser,
    storageDirs: StorageDirs,
    val platformImpl: IPlatformImpl
) : IScreenshotService, IColorScreenshotProvider {
    private var buffer: ByteArray? = null
    private val imgPath = File(storageDirs.storageRoot, "sshot.raw").absolutePath

    private var rootLoadMat: Mat? = null
    private val rootConvertMat = Mat()
    private val pattern = DroidCvPattern(rootConvertMat, false)

    private fun screenshotIntoBuffer() {
        SuperUser.sendCommand("/system/bin/screencap $imgPath")

        FileInputStream(imgPath).use {
            DataInputStream(it).use { reader ->
                val w = reader.readIntLE()
                val h = reader.readIntLE()
                val format = reader.readIntLE()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    reader.readIntLE()
                }

                if (buffer == null) {
                    // If format is not RGBA, notify
                    if (format != 1) {
                        platformImpl.toast("Unexpected raw image format: $format")
                    }

                    Timber.debug { "${w}x${h} format=$format" }

                    buffer = ByteArray(w * h * 4)
                    rootLoadMat = Mat(h, w, CvType.CV_8UC4)
                }

                buffer?.let { b -> reader.read(b, 0, b.size) }
            }
        }

        rootLoadMat?.put(0, 0, buffer)
    }

    override fun takeScreenshot(): IPattern {
        screenshotIntoBuffer()

        Imgproc.cvtColor(rootLoadMat, rootConvertMat, Imgproc.COLOR_RGBA2GRAY)

        return pattern
    }

    override fun takeColorScreenshot(): IPattern {
        screenshotIntoBuffer()

        val mat = Mat()
        Imgproc.cvtColor(rootLoadMat, mat, Imgproc.COLOR_RGBA2BGR)

        return DroidCvPattern(mat)
    }

    override fun close() {
        rootLoadMat?.release()
        rootConvertMat.release()

        try {
            SuperUser.close()
        } catch (e: Exception) {
            Timber.error(e) { "Error closing super user" }
        }

        pattern.close()

        buffer = null
    }

    override fun startRecording(): AutoCloseable? = null
}