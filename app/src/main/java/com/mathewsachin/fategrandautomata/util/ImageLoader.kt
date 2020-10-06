package com.mathewsachin.fategrandautomata.util

import android.content.Context
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.imaging.DroidCvPattern
import com.mathewsachin.fategrandautomata.scripts.IImageLoader
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.ScriptExitException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.inject.Inject

class ImageLoader @Inject constructor(
    val storageDirs: StorageDirs,
    val prefs: IPreferences,
    @ApplicationContext val context: Context
) : IImageLoader {
    private fun fileLoader(FileName: String): IPattern? {
        val filepath = File(storageDirs.supportImgFolder, FileName)

        if (filepath.exists()) {
            val inputStream = FileInputStream(filepath)

            inputStream.use {
                return DroidCvPattern(it).tag(FileName)
            }
        }

        return null
    }

    private fun createPattern(gameServer: GameServerEnum, FileName: String): IPattern {
        val filePath = "$gameServer/${FileName}"

        val assets = context.assets

        val inputStream = assets.open(filePath)

        inputStream.use {
            return DroidCvPattern(it).tag(filePath)
        }
    }

    private var currentGameServer: GameServerEnum =
        GameServerEnum.En
    private var regionCachedPatterns = mutableMapOf<String, IPattern>()

    override fun loadRegionPattern(path: String): IPattern {
        val server = prefs.gameServer

        // Reload Patterns on Server change
        if (currentGameServer != server) {
            clearImageCache()

            currentGameServer = server
        }

        if (!regionCachedPatterns.containsKey(path)) {
            val pattern =
                loadPatternWithFallback(path)

            regionCachedPatterns[path] = pattern
        }

        return regionCachedPatterns[path]!!
    }

    /**
     * When image is not available for the current server, use the image from NA server.
     */
    private fun loadPatternWithFallback(path: String): IPattern {
        if (currentGameServer != GameServerEnum.En) {
            return try {
                createPattern(currentGameServer, path)
            } catch (e: FileNotFoundException) {
                createPattern(GameServerEnum.En, path)
            }
        }

        return createPattern(currentGameServer, path)
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
                ?: throw ScriptExitException(
                    context.getString(R.string.support_img_not_found, path, storageDirs.supportImgFolder)
                )

            supportCachedPatterns[path] = pattern
        }

        return supportCachedPatterns[path]!!
    }
}