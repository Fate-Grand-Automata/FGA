package io.github.fate_grand_automata.util

import android.content.Context
import android.content.res.AssetManager
import io.github.fate_grand_automata.SupportImageKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

class SupportImageExtractor(
    val context: Context,
    val storageProvider: StorageProvider
) {
    private val SupportImageKind.assetFolder
        get() = "Support/" + when (this) {
            SupportImageKind.Servant -> "servant"
            SupportImageKind.CE -> "ce"
            SupportImageKind.Friend -> "friend"
        }

    private fun extract(kind: SupportImageKind) {
        val assetFolder = kind.assetFolder
        val assets = context.assets
        val writer = storageProvider.supportImageWriter(kind)

        for (assetFileName in assets.list(assetFolder)!!) {
            val assetPath = "${assetFolder}/$assetFileName"
            val subFiles = assets.list(assetPath) ?: emptyArray()

            // This is a folder
            if (subFiles.isNotEmpty()) {
                for (subFileName in subFiles) {
                    val subAssetPath = "${assetPath}/$subFileName"
                    val subOutName = "$assetFileName/$subFileName"

                    copyAssetToFile(
                        assets,
                        subAssetPath,
                        writer,
                        subOutName
                    )
                }
            } else {
                copyAssetToFile(
                    assets,
                    assetPath,
                    writer,
                    assetFileName
                )
            }
        }
    }

    private fun copyAssetToFile(Assets: AssetManager, AssetPath: String, writer: SupportImageWriter, fileName: String) {
        // this should prevent
        val originalDigest = MessageDigest.getInstance("SHA-256")
        val copiedFileDigest = MessageDigest.getInstance("SHA-256")
        do {
            originalDigest.reset()
            copiedFileDigest.reset()

            val assetStream = DigestInputStream(Assets.open(AssetPath), originalDigest)
            assetStream.use {
                val outStream = DigestOutputStream(writer.write(fileName), copiedFileDigest)
                outStream.use {
                    assetStream.copyTo(outStream)
                }
            }
            if (!MessageDigest.isEqual(originalDigest.digest(), copiedFileDigest.digest())) {
                Timber.w("Digests were not equal")
            }
        } while (!MessageDigest.isEqual(originalDigest.digest(), copiedFileDigest.digest()))

    }

    suspend fun extract() =
        // Concurrent extractions would both create missing files, leading to "name (1).png" duplicates
        extractionLock.withLock {
            withContext(Dispatchers.IO) {
                storageProvider.createNoMediaFile()
                extract(SupportImageKind.Servant)
                extract(SupportImageKind.CE)
                storageProvider.markSupportImagesExtracted()
            }
        }

    companion object {
        private val extractionLock = Mutex()
    }
}