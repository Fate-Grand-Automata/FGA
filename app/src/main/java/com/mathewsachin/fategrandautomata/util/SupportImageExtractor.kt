package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.content.res.AssetManager
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.SupportImageKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest

class SupportImageExtractor(
    val context: Context,
    val storageProvider: IStorageProvider
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
                        kind,
                        subOutName
                    )
                }
            } else {
                copyAssetToFile(
                    assets,
                    assetPath,
                    kind,
                    assetFileName
                )
            }
        }
    }

    private fun copyAssetToFile(Assets: AssetManager, AssetPath: String, kind: SupportImageKind, fileName: String) {
        // this should prevent
        val originalDigest = MessageDigest.getInstance("SHA-256")
        val copiedFileDigest = MessageDigest.getInstance("SHA-256")
        do {
            originalDigest.reset()
            copiedFileDigest.reset()

            val assetStream = DigestInputStream(Assets.open(AssetPath), originalDigest)
            assetStream.use {
                val outStream = DigestOutputStream(storageProvider.writeSupportImage(kind, fileName), copiedFileDigest)
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
        withContext(Dispatchers.IO) {
            extract(SupportImageKind.Servant)
            extract(SupportImageKind.CE)
        }
}