package com.mathewsachin.fategrandautomata.scripts

import android.content.res.AssetManager
import android.os.Environment
import com.mathewsachin.fategrandautomata.util.AutomataApplication
import com.mathewsachin.fategrandautomata.core.AutomataApi
import com.mathewsachin.fategrandautomata.core.IPattern
import com.mathewsachin.fategrandautomata.core.ScriptExitException
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

val storageDir: File by lazy{
    var dir = Environment.getExternalStorageDirectory()
    dir = File(dir, "Fate-Grand-Automata")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

val supportImgFolder: File by lazy{
    val dir = File(storageDir, "support")

    if (!dir.exists()) {
        dir.mkdirs()

        // Create a .nomedia file so the images won't be added to gallery apps
        val noMediaFile = File(dir, ".nomedia")
        noMediaFile.createNewFile()
    }

    dir
}

val supportServantImgFolder: File by lazy{
    val dir = File(supportImgFolder, "servant")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

val supportCeFolder: File by lazy{
    val dir = File(supportImgFolder, "ce")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

private fun fileLoader(FileName: String): IPattern? {
    val filepath = File(supportImgFolder, FileName)

    if (filepath.exists()) {
        val inputStream = FileInputStream(filepath)

        inputStream.use {
            return AutomataApi.loadPattern(it)
        }
    }

    return null
}

private fun createPattern(FilePath: String): IPattern {
    val assets = AutomataApplication.Instance.assets

    val inputStream = assets.open(FilePath)

    inputStream.use {
        return AutomataApi.loadPattern(it)
    }
}

private var currentGameServer: GameServerEnum = GameServerEnum.En
private var regionCachedPatterns = mutableMapOf<String, IPattern>()

fun getRegionPattern(FileName: String): IPattern {
    val server = Preferences.GameServer

    // Reload Patterns on Server change
    if (currentGameServer != server) {
        clearImageCache()

        currentGameServer = server
    }

    if (!regionCachedPatterns.containsKey(FileName)) {
        val pattern = createPattern("${currentGameServer}/${FileName}")

        regionCachedPatterns[FileName] = pattern
    }

    return regionCachedPatterns[FileName]!!
}

fun clearImageCache(){
    for (pattern in regionCachedPatterns.values) {
        pattern.close()
    }

    regionCachedPatterns.clear()

    clearSupportCache()
}

private var supportCachedPatterns = mutableMapOf<String, IPattern>()

fun clearSupportCache() {
    for (pattern in supportCachedPatterns.values) {
        pattern.close()
    }

    supportCachedPatterns.clear()
}

fun loadSupportImagePattern(FileName: String): IPattern {
    if (!supportCachedPatterns.containsKey(FileName)) {
        val pattern = fileLoader(FileName)
            ?: throw ScriptExitException("Unable to load image: $FileName. Put images in $supportImgFolder folder")

        supportCachedPatterns[FileName] = pattern
    }

    return supportCachedPatterns[FileName]!!
}

private fun supportImgExtractor(FolderName: String) {
    val assetFolder = "Support/$FolderName"
    val outDir = File(supportImgFolder, FolderName)

    if (!outDir.exists()) {
        outDir.mkdirs()
    }

    val assets = AutomataApplication.Instance.assets

    for (assetFileName in assets.list(assetFolder)) {
        var assetPath = "${assetFolder}/$assetFileName"
        val outPath = File(outDir, assetFileName)

        var subFiles = assets.list(assetPath)

        // This is a folder
        if (subFiles.isNotEmpty()) {
            if (!outPath.exists()) {
                outPath.mkdirs()
            }

            for (subFileName in subFiles) {
                val subAssetPath = "${assetPath}/$subFileName"
                var subOutPath = File(outPath, subFileName)

                copyAssetToFile(assets, subAssetPath, subOutPath)
            }
        }
        else {
            copyAssetToFile(assets, assetPath, outPath)
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

suspend fun extractSupportImgs() = withContext(Dispatchers.IO) {
    supportImgExtractor("servant")
    supportImgExtractor("ce")
}