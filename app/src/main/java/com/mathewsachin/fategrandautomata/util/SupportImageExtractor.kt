package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.content.res.AssetManager
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.SupportImageKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        val assetStream = Assets.open(AssetPath)
        assetStream.use {
            val outStream = storageProvider.writeSupportImage(kind, fileName)
            outStream.use {
                assetStream.copyTo(outStream)
            }
        }
    }

    suspend fun extract() =
        withContext(Dispatchers.IO) {
            extract(SupportImageKind.Servant)
            extract(SupportImageKind.CE)
        }
}