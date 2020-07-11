package com.mathewsachin.fategrandautomata.root

import android.os.Build
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.IScreenshotService
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.storageDir
import com.mathewsachin.fategrandautomata.util.readIntLE
import com.mathewsachin.libautomata.IPlatformImpl
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

class RootScreenshotService(
    private val SuperUser: SuperUser,
    val platformImpl: IPlatformImpl
) : IScreenshotService {
    private var buffer: ByteArray? = null
    private val imgPath = File(storageDir, "sshot.raw").absolutePath

    private var rootLoadMat: Mat? = null
    private val rootConvertMat = Mat()
    private val pattern = DroidCvPattern(rootConvertMat,false)

    override fun takeScreenshot(): IPattern {
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

                    buffer = ByteArray(w * h * 4)
                    rootLoadMat = Mat(h, w, CvType.CV_8UC4)
                }

                reader.read(buffer, 0, buffer!!.size)
            }
        }

        rootLoadMat?.put(0, 0, buffer)

        Imgproc.cvtColor(rootLoadMat, rootConvertMat, Imgproc.COLOR_RGBA2GRAY)

        return pattern
    }

    override fun close() {
        rootLoadMat?.release()
        rootConvertMat.release()

        pattern.close()

        buffer = null
    }
}