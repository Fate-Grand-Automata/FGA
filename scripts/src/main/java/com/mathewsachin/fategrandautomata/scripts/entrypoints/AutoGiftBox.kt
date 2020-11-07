package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.ISwipeLocations
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.seconds

class AutoGiftBox @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi,
    val swipeLocations: ISwipeLocations
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    companion object {
        const val maxClickCount = 99
        const val maxNullStreak = 3
        val checkRegion = Region(1640, 400, 120, 2120)
        val scrollEndRegion = Region(1820, 1421, 120, 19)
    }

    override fun script(): Nothing {
        val swipeLocation = swipeLocations.giftBox
        var clickCount = 0
        var aroundEnd = false
        var nullStreak = 0

        while (clickCount < maxClickCount) {
            val picked = pickGifts()
            clickCount += picked

            if (!aroundEnd) {
                // The scrollbar end position matches before completely at end
                // a few items can be left off if we're not careful
                aroundEnd = images.giftBoxScrollEnd in scrollEndRegion
            }

            swipe(swipeLocation.start, swipeLocation.end)

            if (aroundEnd) {
                // Once we're around the end, stop after we don't pick anything consecutively
                if (picked == 0) {
                    ++nullStreak
                } else nullStreak = 0

                if (nullStreak >= maxNullStreak) {
                    break
                }

                // Longer animations. At the end, items pulled up and released.
                1.seconds.wait()
            }
        }

        throw ScriptExitException(messages.pickedExpStack(clickCount))
    }

    private val countRegionX
        get() = when (prefs.gameServer) {
            GameServerEnum.Jp -> 660
            GameServerEnum.En -> 800
            GameServerEnum.Kr -> 670
            GameServerEnum.Tw -> 700
            else -> throw ScriptExitException("Not supported on this server yet")
        }

    // Return picked count
    private fun pickGifts(): Int {
        var clickCount = 0

        for (gift in checkRegion.findAll(images.giftBoxCheck).sorted()) {
            val countRegion = Region(countRegionX, gift.Region.Y - 120, 300, 100)
            val iconRegion = Region(190, gift.Region.Y - 116, 300, 240)
            val clickSpot = Location(1700, gift.Region.Y + 50)

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
                        countRegion.exists(pattern, Similarity = 0.87)
                    }?.key

                    if (count == null || count > prefs.maxGoldEmberSetSize) {
                        continue
                    }
                }

                clickSpot.click()
                ++clickCount
            }
        }

        return clickCount
    }
}