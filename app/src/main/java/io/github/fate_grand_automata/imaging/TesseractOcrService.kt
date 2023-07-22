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

    protected fun finalize() {
        tessApi.recycle()
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