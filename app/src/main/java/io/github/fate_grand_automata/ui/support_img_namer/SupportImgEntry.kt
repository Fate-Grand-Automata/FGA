package io.github.fate_grand_automata.ui.support_img_namer

import android.net.Uri
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.util.StorageProvider
import java.io.File

class SupportImgEntry(
    val imgPath: File,
    val kind: SupportImageKind,
    val frame: View,
) {
    val checkBox: CheckBox = frame.findViewById(R.id.support_img_check)
    val imgView: ImageView = frame.findViewById(R.id.support_img)
    val textBox: EditText = frame.findViewById(R.id.support_img_txt)
    val errorTxt: TextView = frame.findViewById(R.id.support_img_error)

    companion object {
        // *, ?, \, |, / are special characters in Regex and need to be escaped using \
        private const val INVALID_CHARS = """<>"\|:\*\?\\\/"""
        private const val FILE_NAME_REGEX = """[^\.\s$INVALID_CHARS][^$INVALID_CHARS]*"""

        val regex = Regex("""$FILE_NAME_REGEX(/$FILE_NAME_REGEX)?""")
        private const val INVALID_CHARS_MSG = "<, >, \", |, :, *, ?, \\, /"
    }

    private val invalidMsg = frame.context.getString(R.string.support_img_namer_invalid_message, INVALID_CHARS_MSG)

    init {
        if (!imgPath.exists()) {
            hide()
        } else {
            imgView.setImageURI(Uri.parse(imgPath.absolutePath))

            // Allow clicking the image to toggle the checkbox too for convenience
            imgView.setOnClickListener {
                checkBox.toggle()
            }

            textBox.visibility = View.GONE

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                // Hide text field when not checked to prevent confusion for users
                textBox.visibility = if (isChecked) View.VISIBLE else View.GONE

                // Focus only, don't open soft keyboard
                if (isChecked) {
                    textBox.requestFocus()
                }
            }
        }
    }

    fun hide() {
        frame.visibility = View.GONE
    }

    private fun showAlert(Msg: String) {
        errorTxt.text = Msg
        errorTxt.visibility = View.VISIBLE
    }

    fun isValid(): Boolean {
        if (!checkBox.isChecked) {
            return true
        }

        val oldPath = imgPath
        val newFileName = textBox.text.toString()

        if (!oldPath.exists()) {
            // Either the file was deleted or not generated in the first place.
            return true
        }

        val context = frame.context

        if (newFileName.isBlank()) {
            showAlert(context.getString(R.string.support_img_namer_blank_file_name))
            return false
        }

        if (!regex.matches(newFileName)) {
            showAlert(invalidMsg)
            return false
        }

        return true
    }

    fun rename(storageProvider: StorageProvider): Boolean {
        errorTxt.visibility = View.GONE

        if (!checkBox.isChecked) {
            return true
        }

        val oldPath = imgPath
        val newFileName = textBox.text.toString()

        if (!oldPath.exists()) {
            // Either the file was deleted or not generated in the first place.
            return true
        }

        try {
            storageProvider.writeSupportImage(kind, "$newFileName.png").use { outStream ->
                oldPath.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }

            oldPath.delete()
        } catch (e: Exception) {
            val context = frame.context
            showAlert(context.getString(R.string.support_img_namer_file_rename_failed, newFileName))
            return false
        }

        hide()

        return true
    }
}
