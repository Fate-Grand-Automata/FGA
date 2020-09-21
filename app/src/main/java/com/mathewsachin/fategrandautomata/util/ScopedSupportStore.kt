package com.mathewsachin.fategrandautomata.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.mathewsachin.fategrandautomata.SupportStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import javax.inject.Inject

// TODO: This seems to work, but feels really Hacky
@RequiresApi(Build.VERSION_CODES.Q)
class ScopedSupportStore @Inject constructor(
    @ApplicationContext val context: Context
) : SupportStore {
    class ScopedSupportFile(
        name: String,
        val uri: Uri,
        val context: Context,
        val relativePath: String,
        kind: String
    ) : SupportStore.SupportImage.File(name, kind) {
        override fun read() =
            context.contentResolver.openInputStream(uri) ?: throw Exception("Can't open '$name'")

        override val nameWithoutExtension: String
            get() = name.split('.')[0]
    }

    class ScopedSupportDir(name: String, kind: String, override val list: List<ScopedSupportFile>) : SupportStore.SupportImage.Dir(name, kind)

    companion object {
        val baseServantPath =
            "${Environment.DIRECTORY_PICTURES}/${LegacySupportStore.subFolder}/${LegacySupportStore.servant}/"
    }

    override val servants: List<SupportStore.SupportImage>
        get() = queryFiles(LegacySupportStore.servant, LegacySupportStore.servant)
            .groupBy { it.relativePath }
            .flatMap { (key, list) ->
                if (key == baseServantPath) {
                    list
                } else listOf(
                    ScopedSupportDir(
                        key.split('/').last { it.isNotBlank() },
                        LegacySupportStore.servant,
                        list
                    )
                )
            }

    override val ces get() = queryFiles(LegacySupportStore.ce, LegacySupportStore.ce)
    override val friends get() = queryFiles(LegacySupportStore.friend, LegacySupportStore.friend)

    private fun queryFiles(path: String, kind: String): List<ScopedSupportFile> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf(
            "${Environment.DIRECTORY_PICTURES}/${LegacySupportStore.subFolder}/$path%"
        )
        val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"

        val resolver = context.contentResolver
        val query = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        val items = mutableListOf<ScopedSupportFile>()

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val relativePath = cursor.getString(relativePathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                items += ScopedSupportFile(name, contentUri, context, relativePath, kind)
            }
        }

        return items
    }

    private fun addImage(path: String, name: String): OutputStream {
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/${LegacySupportStore.subFolder}/$path"
            )
        }

        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
            resolver.openOutputStream(uri)
        } ?: throw Exception("Couldn't add image: '$path/$name'")
    }

    override fun addServant(name: String) =
        if (name.contains('/')) {
            val split = name.split('/')

            addImage("${LegacySupportStore.servant}/${split[0]}", split[1])
        } else addImage(LegacySupportStore.servant, name)

    override fun addCE(name: String) =
        addImage(LegacySupportStore.ce, name)

    override fun addFriend(name: String) =
        addImage(LegacySupportStore.friend, name)

    override fun getServants(name: String): List<SupportStore.SupportImage.File> {
        return servants.first { it.name == name }.let {
            when (it) {
                is SupportStore.SupportImage.File -> listOf(it)
                is SupportStore.SupportImage.Dir -> it.list
            }
        }
    }

    override fun getCE(name: String) = ces.first { it.name == name }

    override fun getFriend(name: String) = friends.first { it.name == name }
}