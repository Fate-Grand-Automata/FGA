package com.mathewsachin.fategrandautomata.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.mathewsachin.fategrandautomata.scripts.IDropScreenshotStore
import com.mathewsachin.libautomata.IPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.Q)
class ScopedDropScreenshotStore @Inject constructor(
    @ApplicationContext val context: Context
) : IDropScreenshotStore {
    override fun insert(images: List<IPattern>) {
        val resolver = context.contentResolver

        val sdf = SimpleDateFormat("dd-M-yyyy-hh-mm-ss", Locale.US)
        val timeString = sdf.format(Date())

        for ((i, image) in images.withIndex()) {
            val dropFileName = "${timeString}.${i}.png"

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, dropFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/FGA/drops")
            }

            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                resolver.openOutputStream(uri)?.use { stream ->
                    image.use { it.save(stream) }
                }
            }
        }
    }
}