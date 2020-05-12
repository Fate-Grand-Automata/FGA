package com.mathewsachin.fategrandautomata.ui.support_img_namer

import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.mathewsachin.fategrandautomata.util.AutomataApplication
import java.io.File

abstract class SupportImgEntry(
    val ImgPath: File,
    val ImgView: ImageView,
    val DeleteBtn: ImageButton,
    val TextBox: EditText
) {
    init {
        if (!ImgPath.exists()) {
            hide()
        }
        else {
            ImgView.setImageURI(Uri.parse(ImgPath.absolutePath))

            DeleteBtn.setOnClickListener { delete() }
        }
    }

    private fun delete() {
        AlertDialog.Builder(AutomataApplication.Instance)
            .setMessage("Are you sure you want to delete this image?")
            .setTitle("Confirm Deletion")
            .setPositiveButton("Delete") { _, _ ->
                ImgPath.delete()
                hide()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun hide() {
        ImgView.visibility = View.GONE
        DeleteBtn.visibility = View.GONE
        TextBox.visibility = View.GONE

        TextBox.setText("")
    }

    abstract val regex: Regex
    abstract val invalidMsg: String

    private fun showAlert(Msg: String) {
        AlertDialog.Builder(AutomataApplication.Instance)
            .setTitle("Error")
            .setMessage(Msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    fun isValid(): Boolean {
        val oldPath = ImgPath
        val newFileName = TextBox.text.toString()

        if (!oldPath.exists())
        {
            // Either the file was deleted or not generated in the first place.
            return true
        }

        if (newFileName.isBlank())
        {
            showAlert("One of the names is still empty. Either delete the unnamed Servant/CE or specify a name.")
            return false
        }

        if (!regex.matches(newFileName))
        {
            showAlert(invalidMsg)
            return false
        }

        val folder = oldPath.parentFile
        val newPath = File(folder, "${newFileName}.png")

        if (newPath.exists())
        {
            showAlert("'${newFileName}' already exists. Specify another name.")
            return false
        }

        return true
    }

    fun rename(): Boolean {
        val oldPath = ImgPath
        val newFileName = TextBox.text.toString()

        if (!oldPath.exists())
        {
            // Either the file was deleted or not generated in the first place.
            return true
        }

        val folder = oldPath.parentFile
        val newPath = File(folder, "${newFileName}.png")

        try
        {
            val newPathDir = newPath.parentFile

            if (!newPathDir.exists())
            {
                newPathDir.mkdirs()
            }

            // move
            oldPath.copyTo(newPath)
            oldPath.delete()
        }
        catch (e: Exception)
        {
            showAlert("Failed to rename to: '${newFileName}'")
            return false
        }

        hide()

        return true
    }
}