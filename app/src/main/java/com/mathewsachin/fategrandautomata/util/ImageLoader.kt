package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import com.mathewsachin.libautomata.AutomataApi
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.ScriptExitException
import java.io.File
import java.io.FileInputStream

class ImageLoader(
    val storageDirs: StorageDirs,
    val context: Context
) : IImageLoader {
    private fun fileLoader(FileName: String): IPattern? {
        val filepath = File(storageDirs.supportImgFolder, FileName)

        if (filepath.exists()) {
            val inputStream = FileInputStream(filepath)

            inputStream.use {
                return AutomataApi.PlatformImpl.loadPattern(it, FileName)
            }
        }

        return null
    }

    private fun createPattern(FilePath: String): IPattern {
        val assets = context.assets

        val inputStream = assets.open(FilePath)

        inputStream.use {
            return AutomataApi.PlatformImpl.loadPattern(it, FilePath)
        }
    }

    private var currentGameServer: GameServerEnum =
        GameServerEnum.En
    private var regionCachedPatterns = mutableMapOf<String, IPattern>()

    override fun loadRegionPattern(path: String): IPattern {
        val server = Preferences.gameServer

        // Reload Patterns on Server change
        if (currentGameServer != server) {
            clearImageCache()

            currentGameServer = server
        }

        if (!regionCachedPatterns.containsKey(path)) {
            val pattern =
                createPattern("$currentGameServer/${path}")

            regionCachedPatterns[path] = pattern
        }

        return regionCachedPatterns[path]!!
    }

    override fun clearImageCache() {
        for (pattern in regionCachedPatterns.values) {
            pattern.close()
        }

        regionCachedPatterns.clear()

        clearSupportCache()
    }

    private var supportCachedPatterns = mutableMapOf<String, IPattern>()

    override fun clearSupportCache() {
        for (pattern in supportCachedPatterns.values) {
            pattern.close()
        }

        supportCachedPatterns.clear()
    }

    override fun loadSupportPattern(path: String): IPattern {
        if (!supportCachedPatterns.containsKey(path)) {
            val pattern = fileLoader(path)
                ?: throw ScriptExitException("Unable to load image: $path. Put images in ${storageDirs.supportImgFolder} folder")

            supportCachedPatterns[path] = pattern
        }

        return supportCachedPatterns[path]!!
    }
}