package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.content.res.AssetManager
import com.mathewsachin.fategrandautomata.SupportStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

class SupportImageExtractor(
    val context: Context,
    val supportStore: SupportStore
) {
    private fun extract(folderName: String, inserter: (String) -> OutputStream) {
        val assetFolder = "Support/$folderName"

        val assets = context.assets

        for (assetFileName in assets.list(assetFolder)!!) {
            val assetPath = "${assetFolder}/$assetFileName"

            val subFiles = assets.list(assetPath) ?: emptyArray()

            // This is a folder
            if (subFiles.isNotEmpty()) {
                for (subFileName in subFiles) {
                    val subAssetPath = "${assetPath}/$subFileName"

                    copyAssetToFile(
                        assets,
                        subAssetPath,
                        inserter("${assetFileName}/${subFileName}")
                    )
                }
            } else {
                copyAssetToFile(
                    assets,
                    assetPath,
                    inserter(assetFileName)
                )
            }
        }
    }

    private fun copyAssetToFile(Assets: AssetManager, AssetPath: String, outStream: OutputStream) {
        val assetStream = Assets.open(AssetPath)
        assetStream.use {
            outStream.use {
                assetStream.copyTo(outStream)
            }
        }
    }

    suspend fun extract() =
        withContext(Dispatchers.IO) {
            extract("servant") { supportStore.addServant(it) }
            extract("ce") { supportStore.addCE(it) }
        }
}