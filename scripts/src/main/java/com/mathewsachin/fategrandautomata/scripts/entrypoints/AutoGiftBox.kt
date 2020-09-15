package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.*
import javax.inject.Inject

class AutoGiftBox @Inject constructor(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFgoAutomataApi by fgAutomataApi {

    companion object {
        const val goldThreshold = 3
        const val maxClickCount = 99
        val checkRegion = Region(820, 225, 60, 1050)
    }

    private var clickCount = 0

    override fun script(): Nothing {
        while (clickCount < maxClickCount) {
            checkGifts()

            swipe(Location(700, 700), Location(700, 175))
        }

        throw ScriptExitException("Picked $clickCount EXP stacks")
    }

    private val countRegionX
        get() = when (prefs.gameServer) {
            GameServerEnum.Jp -> 330
            GameServerEnum.En -> 400
            else -> 420
        }

    private fun checkGifts() {
        for (gift in checkRegion.findAll(images.giftBoxCheck)) {
            val countRegion = Region(countRegionX, gift.Region.Y - 50, 100, 30)
            val iconRegion = Region(95, gift.Region.Y - 58, 200, 120)
            val clickSpot = Location(850, gift.Region.Y + 25)

            val gold = images.goldXP in iconRegion
            val silver = !gold && images.silverXP in iconRegion

            if (gold || silver) {
                if (gold) {
                    val count = mapOf(
                        1 to images.x1,
                        2 to images.x2,
                        3 to images.x3,
                        4 to images.x4
                    ).entries.firstOrNull { (_, pattern) ->
                        pattern in iconRegion
                    }?.key

                    if (count == null || count > goldThreshold) {
                        continue
                    }
                }

                clickSpot.click()
                ++clickCount
            }
        }
    }
}