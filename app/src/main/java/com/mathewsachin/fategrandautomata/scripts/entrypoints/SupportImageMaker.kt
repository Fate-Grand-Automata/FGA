package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
import com.mathewsachin.fategrandautomata.scripts.modules.supportRegionToolSimilarity
import com.mathewsachin.fategrandautomata.util.AutomataApplication
import com.mathewsachin.libautomata.*
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

class SupportImageMaker(private var Callback: (() -> Unit)?) : EntryPoint() {
    override fun script(): Nothing {
        initScaling()

        cleanExtractFolder()

        val isInSupport = isInSupport()

        // the servant and CE images are further to the right in the friend screen
        val supportBoundX = if (isInSupport) 53 * 2 else 88 * 2
        var supportBound = Region(supportBoundX, 0, 143 * 2, 110 * 2)
        val searchRegion = Region(2100, 0, 370, 1440)

        val regionAnchor = ImageLocator.SupportRegionTool
        // At max two Servant+CE are completely on screen, so only those
        val regionArray = searchRegion.findAll(regionAnchor, supportRegionToolSimilarity).take(2)

        val screenBounds = Region(0, 0, Game.ScriptSize.Width, Game.ScriptSize.Height)

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

        if (regionArray.count() == 0) {
            throw ScriptExitException("No support images were found on the current screen. Are you on Support selection or Friend list screen?")
        }

        Callback?.invoke()
        Callback = null

        throw ScriptExitException()
    }

    private fun cleanExtractFolder() {
        for (file in supportImgTempDir.listFiles()) {
            file.delete()
        }
    }

    private fun extractServantImage(supportBoundImage: IPattern, i: Int) {
        val servant = supportBoundImage.crop(Region(0, 0, 125, 44))
        servant.use {
            servant.save(
                getServantImgPath(i).absolutePath
            )
        }
    }

    private fun extractCeImage(supportRegionImage: IPattern, i: Int) {
        val ce = supportRegionImage.crop(Region(0, 80, supportRegionImage.width, 25))
        ce.use {
            ce.save(
                getCeImgPath(i).absolutePath
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
                getFriendImgPath(i).absolutePath
            )
        }
    }
}