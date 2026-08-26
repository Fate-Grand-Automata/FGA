package io.github.fate_grand_automata.imaging

import android.content.Context
import android.content.res.AssetManager
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.lib_automata.OcrService
import io.github.lib_automata.Pattern
import io.github.lib_automata.dagger.ScriptScope
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@ScriptScope
class TesseractOcrService @Inject constructor(
    @ApplicationContext val context: Context
) : OcrService {
    private val lock = Any()

    /**
     * Only created on the first [detectText] call.
     */
    private var tessApi: TessBaseAPI? = null

    private fun api(): TessBaseAPI =
        tessApi ?: TessBaseAPI().also { api ->
            try {
                extractTesseractTrainingData()
                api.init(context.filesDir.absolutePath, "eng")
            } catch (e: Throwable) {
                api.recycle()
                throw e
            }

            tessApi = api
        }

    override fun detectText(pattern: Pattern): String {
        synchronized(lock) {
            val api = api()

            (pattern as DroidCvPattern).asBitmap().use { bmp ->
                api.setImage(bmp)
                api.getHOCRText(0)
                val text = api.utF8Text
                api.clear()
                return text
            }
        }
    }

    /**
     * Releases the native Tesseract context. Idempotent, and a no-op if OCR was never used.
     */
    override fun close() {
        synchronized(lock) {
            tessApi?.recycle()
            tessApi = null
        }
    }

    protected fun finalize() {
        // Safety net for any path that doesn't reach close()
        close()
    }

    private fun extractTesseractTrainingData() {
        val tessDir = File(context.filesDir.absolutePath, "tessdata")
        if (!tessDir.exists()) {
            tessDir.mkdir()
        }
        for (assetFileName in context.assets.list("tessdata")!!) {
            val targetFile = File(tessDir, assetFileName)
            if (!targetFile.exists()) {
                copyFile(context.assets, "tessdata/$assetFileName", File(tessDir, assetFileName))
            }
        }
    }

    private fun copyFile(
        am: AssetManager, assetName: String,
        outFile: File
    ) {
        try {
            am.open(assetName).use { `in` ->
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
}