package com.mathewsachin.fategrandautomata.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mathewsachin.fategrandautomata.BuildConfig
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.libautomata.Pattern
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageProvider @Inject constructor(
    val prefsCore: PrefsCore,
    @ApplicationContext val context: Context
) : IStorageProvider {
    fun DocumentFile?.getOrCreateDir(name: String) =
        this?.findFile(name)?.takeIf { it.isDirectory }
            ?: this?.createDirectory(name)
            ?: throw KnownException(KnownException.Reason.CouldNotCreateDirectory(name))

    fun DocumentFile?.getOrCreateFile(name: String, mime: String = mimeAny) =
        this?.findFile(name)?.takeIf { !it.isDirectory }
            ?: this?.createFile(mime, name)
            ?: throw KnownException(KnownException.Reason.CouldNotCreateFile(name))

    val resolver: ContentResolver = context.contentResolver
    private var dirRoot: DocumentFile? = null

    val mimeAny = "*/*"
    val mimePng = "image/png"

    val rootDirName
        get() = dirRoot?.name

    private val persistablePermission =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    fun setRoot(rootUri: Uri) {
        prefsCore.dirRoot.get().let { prevDir ->
            // Don't release permission if user picked the same directory again
            if (prevDir.isNotBlank() && prevDir != rootUri.toString()) {
                try {
                    resolver.releasePersistableUriPermission(Uri.parse(prevDir), persistablePermission)
                } catch (e: Exception) {
                    Timber.e(e, "Error on releasing persistable URI")
                }
            }
        }

        prefsCore.dirRoot.set(rootUri.toString())
        resolver.takePersistableUriPermission(rootUri, persistablePermission)

        dirRoot = DocumentFile.fromTreeUri(context, rootUri)
    }

    init {
        prefsCore.dirRoot.get().let { dir ->
            if (dir.isNotBlank()) {
                dirRoot = DocumentFile.fromTreeUri(context, Uri.parse(dir))
            }
        }
    }

    override val supportImageTempDir: File by lazy {
        File(context.cacheDir, "support").apply {
            mkdirs()
        }
    }

    private val supportFolderName = "support"

    val shouldExtractSupportImages
        get() = dirRoot?.findFile(supportFolderName) == null

    private val supportImageFolder
        get() = dirRoot.getOrCreateDir(supportFolderName)

    private val supportServantFolder
        get() = supportImageFolder.getOrCreateDir("servant")

    private val supportCEFolder
        get() = supportImageFolder.getOrCreateDir("ce")

    private val supportFriendFolder
        get() = supportImageFolder.getOrCreateDir("friend")

    private val SupportImageKind.imageFolder
        get() = when (this) {
            SupportImageKind.Servant -> supportServantFolder
            SupportImageKind.CE -> supportCEFolder
            SupportImageKind.Friend -> supportFriendFolder
        }

    override fun writeSupportImage(kind: SupportImageKind, name: String): OutputStream {
        val folder = kind.imageFolder

        val indexOfSlash = name.indexOf('/')
        val resolved = if (indexOfSlash == -1) {
            folder.getOrCreateFile(name, mimePng)
        } else {
            val subFolder = name.substring(0 until indexOfSlash)
            val fileName = name.substring(indexOfSlash + 1)

            folder
                .getOrCreateDir(subFolder)
                .getOrCreateFile(fileName, mimePng)
        }

        return resolver.openOutputStream(resolved.uri)
            ?: throw KnownException(
                KnownException.Reason.CouldNotOpenSupportFileForWriting(kind, name)
            )
    }

    override fun readSupportImage(kind: SupportImageKind, name: String): List<InputStream> {
        val file = kind.imageFolder.findFile(name)
            ?: throw KnownException(
                KnownException.Reason.CouldNotOpenSupportFileForReading(kind, name)
            )

        val files = if (file.isDirectory) {
            file.listFiles().takeUnless { it.isEmpty() }
                ?: throw KnownException(KnownException.Reason.SupportFolderIsEmpty(kind, name))
        } else arrayOf(file)

        return files.map {
            resolver.openInputStream(it.uri)
                ?: throw KnownException(
                    KnownException.Reason.CouldNotOpenSupportFileForReading(kind, name)
                )
        }
    }

    override fun list(kind: SupportImageKind) = kind.imageFolder
        .listFiles()
        .mapNotNull { it.name }

    private val dropsFolder
        get() = dirRoot.getOrCreateDir("drops")

    override fun dropScreenshot(patterns: List<Pattern>) {
        val sdf = SimpleDateFormat("dd-M-yyyy-hh-mm-ss", Locale.US)
        val timeString = sdf.format(Date())

        for ((i, pattern) in patterns.withIndex()) {
            val dropFileName = "${timeString}.${i}.png"

            pattern.use {
                val file = dropsFolder.createFile(mimePng, dropFileName)
                    ?: throw KnownException(KnownException.Reason.CouldNotCreateDropScreenshotFile)

                resolver.openOutputStream(file.uri)?.use { stream ->
                    pattern.save(stream)
                }
            }
        }
    }

    override fun dump(name: String, image: Pattern) {
        image.use {
            if (BuildConfig.DEBUG) {
                val file = dirRoot
                    .getOrCreateDir("dump")
                    .getOrCreateFile(name)

                resolver.openOutputStream(file.uri)?.use { stream ->
                    image.save(stream)
                }
            }
        }
    }
}