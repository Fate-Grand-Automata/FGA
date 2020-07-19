package com.mathewsachin.fategrandautomata.util

import android.content.Context
import android.content.res.AssetManager
import com.mathewsachin.fategrandautomata.StorageDirs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SupportImageExtractor(
    val context: Context,
    val storageDirs: StorageDirs
) {
    private fun extract(FolderName: String) {
        val assetFolder = "Support/$FolderName"
        val outDir = File(storageDirs.supportImgFolder, FolderName)

        if (!outDir.exists()) {
            outDir.mkdirs()
        }

        val assets = context.assets

        for (assetFileName in assets.list(assetFolder)!!) {
            val assetPath = "${assetFolder}/$assetFileName"
            val outPath = File(outDir, assetFileName)

            val subFiles = assets.list(assetPath) ?: emptyArray()

            // This is a folder
            if (subFiles.isNotEmpty()) {
                if (!outPath.exists()) {
                    outPath.mkdirs()
                }

                for (subFileName in subFiles) {
                    val subAssetPath = "${assetPath}/$subFileName"
                    val subOutPath = File(outPath, subFileName)

                    copyAssetToFile(
                        assets,
                        subAssetPath,
                        subOutPath
                    )
                }
            } else {
                copyAssetToFile(
                    assets,
                    assetPath,
                    outPath
                )
            }
        }
    }

    private fun copyAssetToFile(Assets: AssetManager, AssetPath: String, OutPath: File) {
        val assetStream = Assets.open(AssetPath)
        assetStream.use {
            val outStream = FileOutputStream(OutPath)
            outStream.use {
                assetStream.copyTo(outStream)
            }
        }
    }

    suspend fun extract() =
        withContext(Dispatchers.IO) {
            extract("servant")
            extract("ce")
        }
}