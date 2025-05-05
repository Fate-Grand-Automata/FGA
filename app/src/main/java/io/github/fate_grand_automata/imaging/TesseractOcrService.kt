package io.github.fate_grand_automata.imaging

import android.content.Context
import android.content.res.AssetManager
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.lib_automata.OcrService
import io.github.lib_automata.Pattern
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@ServiceScoped
class TesseractOcrService @Inject constructor(
    @ApplicationContext val context: Context
) : OcrService, AutoCloseable  {
    private val tessApi = TessBaseAPI()

    init {
        extractTesseractTrainingData()
        tessApi.init(context.filesDir.absolutePath, "eng")
    }

    override fun detectText(pattern: Pattern): String {
        synchronized(tessApi) {
            (pattern as DroidCvPattern).asBitmap().use { bmp ->
                tessApi.setImage(bmp)
                tessApi.getHOCRText(0)
                val text = tessApi.utF8Text
                tessApi.clear()
                return text
            }
        }
    }

    private fun extractTesseractTrainingData() {
        val tessDir = File(context.filesDir.absolutePath, "tessdata")
        if (!tessDir.exists()) {
            tessDir.mkdir()
        }
        for (assetFileName in context.assets.list("tessdata")!!) {
            val targetFile = File(tessDir, assetFileName)
            val assetPath = "tessdata/$assetFileName"
            if (!targetFile.exists()) {
                copyFile(assetPath, targetFile)
            }
        }
    }

    private fun copyFile(
        assetName: String,
        outFile: File
    ) {
        try {
            context.assets.open(assetName).use { `in` ->
                FileOutputStream(outFile).use { out ->
                    val buffer = ByteArray(1024)
                    var read: Int
                    while (`in`.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun close() {
        Timber.d("Closing TesseractOcrService...")

        synchronized(tessApi) {
            try {
                Timber.d("Stopping Tesseract API...")
                tessApi.recycle()
                Timber.i("Tesseract API stopped.")
            } catch (e: Exception) {
                Timber.e(e, "Failed to stop Tesseract API")
            }
        }
        Timber.d("TesseractOcrService closed.")
    }
}