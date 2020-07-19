package com.mathewsachin.fategrandautomata.util

import android.os.Environment
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.libautomata.AutomataApi
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.ScriptExitException
import java.io.File
import java.io.FileInputStream

val storageDir: File by lazy {
    var dir = Environment.getExternalStorageDirectory()
    dir = File(dir, "Fate-Grand-Automata")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

private val supportImgFolderPath: File by lazy { File(storageDir, "support") }

val shouldExtractSupportImages get() = !supportImgFolderPath.exists()

val supportImgFolder: File by lazy {
    val dir = supportImgFolderPath

    if (!dir.exists()) {
        dir.mkdirs()

        // Create a .nomedia file so the images won't be added to gallery apps
        val noMediaFile = File(dir, ".nomedia")
        noMediaFile.createNewFile()
    }

    dir
}

val supportServantImgFolder: File by lazy {
    val dir = File(supportImgFolder, "servant")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

val supportCeFolder: File by lazy {
    val dir = File(supportImgFolder, "ce")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

val supportFriendFolder: File by lazy {
    val dir = File(supportImgFolder, "friend")

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
            return AutomataApi.PlatformImpl.loadPattern(it, FileName)
        }
    }

    return null
}

private fun createPattern(FilePath: String): IPattern {
    val assets = AutomataApplication.Instance.assets

    val inputStream = assets.open(FilePath)

    inputStream.use {
        return AutomataApi.PlatformImpl.loadPattern(it, FilePath)
    }
}

private var currentGameServer: GameServerEnum = GameServerEnum.En
private var regionCachedPatterns = mutableMapOf<String, IPattern>()

fun getRegionPattern(FileName: String): IPattern {
    val server = Preferences.gameServer

    // Reload Patterns on Server change
    if (currentGameServer != server) {
        clearImageCache()

        currentGameServer = server
    }

    if (!regionCachedPatterns.containsKey(FileName)) {
        val pattern =
            createPattern("$currentGameServer/${FileName}")

        regionCachedPatterns[FileName] = pattern
    }

    return regionCachedPatterns[FileName]!!
}

fun clearImageCache() {
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