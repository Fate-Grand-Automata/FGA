package io.github.fate_grand_automata.imaging

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.lib_automata.OcrService
import io.github.lib_automata.Pattern
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject


@ScriptScope
class GoogleMLOcrService @Inject constructor(
    @ApplicationContext val context: Context
) : OcrService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun detectText(pattern: Pattern): String {
        synchronized(recognizer) {
            (pattern as DroidCvPattern).asBitmap().use { bmp ->
                return InputImage.fromBitmap(bmp, 0).let { image ->
                    Tasks.await(recognizer.process(image).addOnFailureListener {
                        it.printStackTrace()
                        ""
                    }).text
                }
            }
        }
    }
}