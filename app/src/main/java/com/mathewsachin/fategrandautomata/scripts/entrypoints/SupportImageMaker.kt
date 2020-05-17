package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
import com.mathewsachin.fategrandautomata.scripts.modules.supportRegionToolSimilarity
import com.mathewsachin.fategrandautomata.scripts.supportCeFolder
import com.mathewsachin.fategrandautomata.scripts.supportServantImgFolder
import java.io.File

fun getServantImgPath(Id: String, Index: Int): File {
    return File(supportServantImgFolder, "${Id}_servant${Index}.png")
}

fun getCeImgPath(Id: String, Index: Int): File {
    return File(supportCeFolder, "${Id}_ce${Index}.png")
}

typealias SupportImageMakerCallback = (String) -> Unit

class SupportImageMaker(private var Callback: SupportImageMakerCallback?) : EntryPoint() {
    override fun script(): Nothing {
        initScaling()

        val isInSupport =
            isInSupport()

        var supportBound = Region(53 * 2, 0, 143 * 2, 110 * 2)
        val regionAnchor =
            ImageLocator.SupportRegionTool

        val searchRegion = Region(2100, 0, 300, 1440)
        val regionArray = searchRegion.findAll(regionAnchor, supportRegionToolSimilarity)

        val screenBounds = Region(0, 0, Game.ScriptSize.Width, Game.ScriptSize.Height)

        val timestamp = System.currentTimeMillis().toString()

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
                        getServantImgPath(
                            timestamp,
                            i
                        ).absolutePath
                    )
                }

                val ce = it.crop(Region(0, 80, pattern.width, 25))
                ce.use {
                    ce.save(
                        getCeImgPath(
                            timestamp,
                            i
                        ).absolutePath
                    )
                }
            }

            ++i
        }

        if (i == 0) {
            throw ScriptExitException("No support images were found on the current screen. Are you on Support selection or Friend list screen?")
        }

        Callback?.invoke(timestamp)

        Callback = null

        throw ScriptExitException("Support Image(s) were generated.")
    }
}