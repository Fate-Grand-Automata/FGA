package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ITemporaryStore
import com.mathewsachin.fategrandautomata.scripts.SupportImageMakerExitException
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.modules.supportRegionToolSimilarity
import com.mathewsachin.libautomata.*
import javax.inject.Inject

class SupportImageMaker @Inject constructor(
    val tempStore: ITemporaryStore,
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFgoAutomataApi by fgAutomataApi {
    companion object {
        fun getServantImgKey(index: Int) =
            "support/servant_${index}.png"

        fun getCeImgKey(index: Int) =
            "support/ce_${index}.png"

        fun getFriendImgKey(index: Int) =
            "support/friend_${index}.png"
    }

    override fun script(): Nothing {
        tempStore.clear()

        val isInSupport = isInSupport()

        // the servant and CE images are further to the right in the friend screen
        val supportBoundX = if (isInSupport) 106 else 176
        val supportBound = Region(supportBoundX, 0, 286, 220)
        val screenBounds = Region(Location(), Game.scriptSize)

        // At max two Servant+CE are completely on screen, so only use those
        val regionArray = Game.supportRegionToolSearchRegion
            .findAll(
                images.supportRegionTool,
                supportRegionToolSimilarity
            )
            .map {
                // in the friend screen, the "Confirm Support Setup" button is higher
                val newSupportBoundY = it.Region.Y + (if (isInSupport) 66 else 82)
                supportBound.copy(Y = newSupportBoundY)
            }
            .filter { it in screenBounds }
            .take(2)
            .toList()

        for ((i, region) in regionArray.withIndex()) {
            region.getPattern().use {
                extractServantImage(it, i)
                extractCeImage(it, i)
                extractFriendNameImage(region, isInSupport, i)
            }
        }

        if (regionArray.isEmpty()) {
            throw ScriptExitException(messages.supportImageMakerNotFound)
        }

        throw SupportImageMakerExitException()
    }

    private fun extractServantImage(supportBoundImage: IPattern, i: Int) {
        val servant = supportBoundImage.crop(Region(0, 0, 125, 44))
        servant.use {
            tempStore.write(getServantImgKey(i)).use { stream ->
                servant.save(stream)
            }
        }
    }

    private fun extractCeImage(supportRegionImage: IPattern, i: Int) {
        val ce = supportRegionImage.crop(Region(0, 80, supportRegionImage.width, 25))
        ce.use {
            tempStore.write(getCeImgKey(i)).use { stream ->
                ce.save(stream)
            }
        }
    }

    private fun extractFriendNameImage(supportBound: Region, isInSupport: Boolean, i: Int) {
        // the friend name is further to the left in the friend screen
        val friendNameX = supportBound.X + (if (isInSupport) 364 else 344)
        val friendBound = Region(friendNameX, supportBound.Y - 95, 400, 110)

        val friendPattern = friendBound.getPattern()
        friendPattern.use {
            tempStore.write(getFriendImgKey(i)).use { stream ->
                friendPattern.save(stream)
            }
        }
    }
}