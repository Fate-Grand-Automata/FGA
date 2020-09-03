package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.SupportImageMakerExitException
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.modules.supportRegionToolSimilarity
import com.mathewsachin.libautomata.*
import java.io.File
import javax.inject.Inject

fun getServantImgPath(dir: File, Index: Int): File {
    return File(dir, "servant_${Index}.png")
}

fun getCeImgPath(dir: File, Index: Int): File {
    return File(dir, "ce_${Index}.png")
}

fun getFriendImgPath(dir: File, Index: Int): File {
    return File(dir, "friend_${Index}.png")
}

class SupportImageMaker @Inject constructor(
    storageDirs: StorageDirs,
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFGAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFGAutomataApi by fgAutomataApi {
    private val dir = storageDirs.supportImgTempDir

    override fun script(): Nothing {
        scaling.init()

        cleanExtractFolder()

        val isInSupport = isInSupport()

        // the servant and CE images are further to the right in the friend screen
        val supportBoundX = if (isInSupport) 106 else 176
        var supportBound = Region(supportBoundX, 0, 286, 220)

        // At max two Servant+CE are completely on screen, so only use those
        val regionArray = Game.supportRegionToolSearchRegion
            .findAll(
                images.supportRegionTool,
                supportRegionToolSimilarity
            )
            .take(2)
            .toList()

        val screenBounds = Region(Location(), Game.scriptSize)

        for ((i, testRegion) in regionArray.map { it.Region }.withIndex()) {
            // in the friend screen, the "Confirm Support Setup" button is higher
            val newSupportBoundY = testRegion.Y + (if (isInSupport) 66 else 82)
            supportBound = supportBound.copy(Y = newSupportBoundY)

            if (supportBound !in screenBounds)
                continue

            supportBound.getPattern().use {
                extractServantImage(it, i)
                extractCeImage(it, i)
                extractFriendNameImage(supportBound, isInSupport, i)
            }
        }

        if (regionArray.isEmpty()) {
            throw ScriptExitException(messages.supportImageMakerNotFound)
        }

        throw SupportImageMakerExitException()
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
                getServantImgPath(dir, i).absolutePath
            )
        }
    }

    private fun extractCeImage(supportRegionImage: IPattern, i: Int) {
        val ce = supportRegionImage.crop(Region(0, 80, supportRegionImage.width, 25))
        ce.use {
            ce.save(
                getCeImgPath(dir, i).absolutePath
            )
        }
    }

    private fun extractFriendNameImage(supportBound: Region, isInSupport: Boolean, i: Int) {
        // the friend name is further to the left in the friend screen
        val friendNameX = supportBound.X + (if (isInSupport) 364 else 344)
        val friendBound = Region(friendNameX, supportBound.Y - 95, 400, 110)

        val friendPattern = friendBound.getPattern()
        friendPattern.use {
            friendPattern.save(
                getFriendImgPath(dir, i).absolutePath
            )
        }
    }
}