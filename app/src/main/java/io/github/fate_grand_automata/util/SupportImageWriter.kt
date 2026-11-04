package io.github.fate_grand_automata.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import io.github.fate_grand_automata.SupportImageKind
import java.io.OutputStream

/**
 * Writes many support images of one [kind] into [folderUri], listing each folder only once.
 *
 * `DocumentFile.findFile` makes one SAF query per entry of the searched folder,
 * which makes resolving folders per image very slow.
 */
class SupportImageWriter(
    private val resolver: ContentResolver,
    private val kind: SupportImageKind,
    private val folderUri: Uri
) {
    private class Child(val uri: Uri, val isDirectory: Boolean)

    private val listings = mutableMapOf<Uri, MutableMap<String, Child>>()

    /**
     * @param name file name, optionally prefixed by one sub-folder: `"name.png"` or `"folder/name.png"`
     */
    fun write(name: String): OutputStream {
        val parts = name.split('/')

        var directoryUri = folderUri
        for (directoryName in parts.dropLast(1)) {
            directoryUri = getOrCreate(directoryUri, directoryName, isDirectory = true)
        }
        val fileUri = getOrCreate(directoryUri, parts.last(), isDirectory = false)

        return resolver.openOutputStream(fileUri)
            ?: throw KnownException(KnownException.Reason.CouldNotOpenSupportFileForWriting(kind, name))
    }

    private fun getOrCreate(parentUri: Uri, name: String, isDirectory: Boolean): Uri {
        // Each folder is queried at most once per writer; later lookups hit the cached listing
        val children = listings.getOrPut(parentUri) { listChildren(parentUri) }

        children[name]
            ?.takeIf { it.isDirectory == isDirectory }
            ?.let { return it.uri }

        /*
         * SAF never overwrites on create: if the name is already taken (e.g. by a file where we
         * need a folder), the provider picks a free name like "name (1)" instead.
         */
        val mimeType = if (isDirectory) Document.MIME_TYPE_DIR else "image/png"
        val created = DocumentsContract.createDocument(resolver, parentUri, mimeType, name)
            ?: throw KnownException(
                if (isDirectory) KnownException.Reason.CouldNotCreateDirectory(name)
                else KnownException.Reason.CouldNotCreateFile(name)
            )

        // Keep the cached listing in sync, so later writes to the same name reuse this document
        children[name] = Child(created, isDirectory)
        return created
    }

    private fun listChildren(directoryUri: Uri): MutableMap<String, Child> {
        // Querying this URI returns one row per entry of the folder
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            directoryUri,
            DocumentsContract.getDocumentId(directoryUri)
        )
        // Fetching every needed column here avoids the per-entry queries DocumentFile.getName() would make
        val projection = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE
        )

        val children = mutableMapOf<String, Child>()
        resolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val uri = DocumentsContract.buildDocumentUriUsingTree(directoryUri, cursor.getString(idColumn))
                val isDirectory = cursor.getString(mimeTypeColumn) == Document.MIME_TYPE_DIR

                children[name] = Child(uri, isDirectory)
            }
        }
        return children
    }
}
