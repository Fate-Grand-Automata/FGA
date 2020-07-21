package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.modules.supportRegionToolSimilarity
import com.mathewsachin.libautomata.*
import java.io.File

fun getServantImgPath(dir: File, Index: Int): File {
    return File(dir, "servant_${Index}.png")
}

fun getCeImgPath(dir: File, Index: Int): File {
    return File(dir, "ce_${Index}.png")
}

fun getFriendImgPath(dir: File, Index: Int): File {
    return File(dir, "friend_${Index}.png")
}

class SupportImageMaker(
    private val dir: File,
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFGAutomataApi,
    private var Callback: (() -> Unit)?
) : EntryPoint(exitManager, platformImpl), IFGAutomataApi by fgAutomataApi {
    override fun script(): Nothing {
        scaling.init()

        cleanExtractFolder()

        val isInSupport =
            isInSupport()

        // the servant and CE images are further to the right in the friend screen
        val supportBoundX = if (isInSupport) 106 else 176
        var supportBound = Region(supportBoundX, 0, 286, 220)
        val searchRegion = Region(2100, 0, 370, 1440)

        val regionAnchor = images.supportRegionTool
        // At max two Servant+CE are completely on screen, so only use those
        val regionArray = searchRegion.findAll(
            regionAnchor,
            supportRegionToolSimilarity
        )
            .take(2).toList()

        val screenBounds = Region(0, 0, game.scriptSize.Width, game.scriptSize.Height)

        for ((i, testRegion) in regionArray.map { it.Region }.withIndex()) {
            // in the friend screen, the "Confirm Support Setup" button is higher
            val newSupportBoundY = testRegion.Y + (if (isInSupport) 66 else 82)
            supportBound = supportBound.copy(Y = newSupportBoundY)

            if (!screenBounds.contains(supportBound))
                continue

            supportBound.getPattern()?.use {
                extractServantImage(it, i)
                extractCeImage(it, i)
                extractFriendNameImage(supportBound, isInSupport, i)
            }
        }

        if (regionArray.isEmpty()) {
            throw ScriptExitException("No support images were found on the current screen. Are you on Support selection or Friend list screen?")
        }

        Callback?.invoke()
        Callback = null

        throw ScriptExitException()
    }

    private fun cleanExtractFolder() {
        dir.listFiles()?.forEach {
            it.delete()
        }
    }

    private fun extractServantImage(supportBoundImage: IPattern, i: Int) {
        val servant = supportBoundImage.crop(Region(0, 0, 125, 44))
        servant.use {
            servant.save(
                getServantImgPath(
                    dir,
                    i
                ).absolutePath
            )
        }
    }

    private fun extractCeImage(supportRegionImage: IPattern, i: Int) {
        val ce = supportRegionImage.crop(Region(0, 80, supportRegionImage.width, 25))
        ce.use {
            ce.save(
                getCeImgPath(
                    dir,
                    i
                ).absolutePath
            )
        }
    }

    private fun extractFriendNameImage(supportBound: Region, isInSupport: Boolean, i: Int) {
        // the friend name is further to the left in the friend screen
        val friendNameX = supportBound.X + (if (isInSupport) 364 else 344)
        val friendBound = Region(friendNameX, supportBound.Y - 95, 400, 110)

        val friendPattern = friendBound.getPattern()
        friendPattern?.use {
            friendPattern.save(
                getFriendImgPath(
                    dir,
                    i
                ).absolutePath
            )
        }
    }
}