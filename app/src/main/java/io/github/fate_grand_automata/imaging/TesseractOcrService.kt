package io.github.fate_grand_automata.imaging

import android.content.Context
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.lib_automata.OcrService
import io.github.lib_automata.Pattern
import timber.log.Timber
import java.security.MessageDigest
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

            val assetHash = calculateAssetHash(assetPath)
            if (assetHash == null) {
                Timber.w("Could not calculate hash for asset: $assetPath. Skipping check.")
                // Fallback to simple existence check or force copy if needed
                if (!targetFile.exists()) {
                        copyFile(assetPath, targetFile)
                } else {
                    Timber.d("File exists, but couldn't verify hash: $targetFile")
                }
                continue // Move to next file
            }

            var needsCopy = true
            if (targetFile.exists()) {
                val targetHash = calculateFileHash(targetFile)
                if (targetHash != null && assetHash == targetHash) {
                    Timber.d("File already exists and hash matches: $targetFile")
                    needsCopy = false
                } else if (targetHash == null) {
                    Timber.w("Could not calculate hash for existing file: $targetFile. Will overwrite.")
                } else {
                    Timber.d("Hash mismatch for $targetFile. Asset hash: $assetHash, File hash: $targetHash. Overwriting.")
                }
            } else {
                Timber.d("File does not exist: $targetFile. Copying.")
            }
            if (needsCopy) {
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

    private fun calculateAssetHash(assetPath: String): String? {
        return try {
            context.assets.open(assetPath).use { inputStream ->
                calculateStreamHash(inputStream)
            }
        } catch (e: IOException) {
            Timber.e(e, "Failed to calculate hash for asset: $assetPath")
            null
        }
    }

    private fun calculateFileHash(file: File): String? {
        return try {
            FileInputStream(file).use { inputStream ->
                calculateStreamHash(inputStream)
            }
        } catch (e: IOException) {
            Timber.e(e, "Failed to calculate hash for file: ${file.absolutePath}")
            null
        }
    }

    private fun calculateStreamHash(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        // Convert byte array to Hex String
        return digest.digest().joinToString("") { "%02x".format(it) }
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