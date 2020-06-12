package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.libautomata.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
import com.mathewsachin.fategrandautomata.scripts.modules.supportRegionToolSimilarity
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import com.mathewsachin.fategrandautomata.util.AutomataApplication
import java.io.File

private val supportImgTempDir: File by lazy {
    val dir = File(AutomataApplication.Instance.cacheDir, "support")

    if (!dir.exists()) {
        dir.mkdirs()
    }

    dir
}

fun getServantImgPath(Index: Int): File {
    return File(supportImgTempDir, "servant_${Index}.png")
}

fun getCeImgPath(Index: Int): File {
    return File(supportImgTempDir, "ce_${Index}.png")
}

fun getFriendImgPath(Index: Int): File {
    return File(supportImgTempDir, "friend_${Index}.png")
}

private fun cleanExtractFolder() {
    for (file in supportImgTempDir.listFiles()) {
        file.delete()
    }
}

class SupportImageMaker(private var Callback: (() -> Unit)?) : EntryPoint() {
    override fun script(): Nothing {
        initScaling()

        cleanExtractFolder()

        val isInSupport =
            isInSupport()

        var supportBound = Region(53 * 2, 0, 143 * 2, 110 * 2)
        val regionAnchor =
            ImageLocator.SupportRegionTool

        val searchRegion = Region(2100, 0, 300, 1440)
        val regionArray = searchRegion.findAll(regionAnchor, supportRegionToolSimilarity)

        val screenBounds = Region(0, 0, Game.ScriptSize.Width, Game.ScriptSize.Height)

        var i = 0

        for (testRegion in regionArray.map { it.Region }) {
            // At max two Servant+CE are completely on screen
            if (i > 1)
                break

            supportBound = if (isInSupport) {
                supportBound.copy(Y = testRegion.Y - 70 + 68 * 2)
            } else {
                // Assume we are on Friend List
                supportBound.copy(X = supportBound.X + 10, Y = testRegion.Y + 82)
            }

            if (!screenBounds.contains(supportBound))
                continue

            val pattern = supportBound.getPattern()

            pattern?.use {
                val servant = it.crop(Region(0, 0, 125, 44))
                servant.use {
                    servant.save(
                        getServantImgPath(i).absolutePath
                    )
                }

                val ce = it.crop(Region(0, 80, pattern.width, 25))
                ce.use {
                    ce.save(
                        getCeImgPath(i).absolutePath
                    )
                }

                val friendBound = Region(supportBound.X + pattern.width + 220, supportBound.Y - 95, 400, 110)
                val friendPattern = friendBound.getPattern()

                friendPattern?.use {
                    friendPattern.save(
                        getFriendImgPath(i).absolutePath
                    )
                }
            }

            ++i
        }

        if (i == 0) {
            throw ScriptExitException("No support images were found on the current screen. Are you on Support selection or Friend list screen?")
        }

        Callback?.invoke()
        Callback = null

        throw ScriptExitException()
    }
}