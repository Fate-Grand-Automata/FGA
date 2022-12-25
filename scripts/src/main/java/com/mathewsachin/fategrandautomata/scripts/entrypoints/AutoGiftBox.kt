package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.modules.ConnectionRetry
import com.mathewsachin.libautomata.*
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class AutoGiftBox @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi,
    private val swipe: Swiper,
    private val connectionRetry: ConnectionRetry
) : EntryPoint(exitManager), IFgoAutomataApi by api {
    sealed class ExitReason {
        object NoEmbersFound : ExitReason()
        class CannotSelectAnyMore(val pickedStacks: Int) : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    companion object {
        const val maxClickCount = 99
        const val maxNullStreak = 3
    }

    private var totalReceived = 0

    override fun script(): Nothing {
        val xpOffsetX = (locations.scriptArea.find(images[Images.GoldXP]) ?: locations.scriptArea.find(images[Images.SilverXP]))
            ?.region?.center?.x
            ?: throw ExitException(ExitReason.NoEmbersFound)

        val checkRegion = Region(xpOffsetX + 1320, 350, 140, 1500)
        val scrollEndRegion = Region(100 + checkRegion.x, 1320, 320, 60)
        val receiveSelectedClick = Location(1890 + xpOffsetX, 750)
        val receiveEnabledRegion = Region(1755 + xpOffsetX, 410, 290, 60)

        while (true) {
            val receiveEnabledPattern = receiveEnabledRegion.getPattern()
            val picked = iteration(checkRegion, scrollEndRegion)
            totalReceived += picked

            if (picked > 0) {
                receiveSelectedClick.click()
                while (true) {
                    2.seconds.wait()
                    if (connectionRetry.needsToRetry()) connectionRetry.retry() else break
                }
                receiveSelectedClick.click()
            } else break

            if (receiveEnabledPattern !in receiveEnabledRegion) break
        }

        throw ExitException(ExitReason.CannotSelectAnyMore(totalReceived))
    }

    private fun iteration(
        checkRegion: Region,
        scrollEndRegion: Region
    ): Int {
        var clickCount = 0
        var aroundEnd = false
        var nullStreak = 0

        while (clickCount < maxClickCount) {
            val picked = useSameSnapIn {
                if (!aroundEnd) {
                    // The scrollbar end position matches before completely at end
                    // a few items can be left off if we're not careful
                    aroundEnd = images[Images.GiftBoxScrollEnd] in scrollEndRegion
                }

                pickGifts(checkRegion)
            }

            clickCount += picked

            swipe(
                locations.giftBoxSwipeStart,
                locations.giftBoxSwipeEnd
            )

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

        /*
           clickCount can be higher than maxClickCount when the script is close to the limit and
           finds multiple collectible stacks on the screen. FGO will not register the extra clicks.
         */
        return clickCount.coerceAtMost(maxClickCount)
    }

    // Return picked count
    private fun pickGifts(checkRegion: Region): Int {
        var clickCount = 0

        for (gift in checkRegion.findAll(images[Images.GiftBoxCheck]).sorted()) {
            val countRegion = when (prefs.gameServer) {
                GameServerEnum.Jp, GameServerEnum.Tw, GameServerEnum.Cn -> -940
                GameServerEnum.En -> -830
                GameServerEnum.Kr -> -1010
            }.let { x -> Region(x, -120, 300, 100) } + gift.region.location

            val iconRegion = Region(-1480, -116, 300, 240) + gift.region.location

            val gold = images[Images.GoldXP] in iconRegion
            val silver = !gold && images[Images.SilverXP] in iconRegion

            if (gold || silver) {
                if (gold) {
                    val count = mapOf(
                        1 to images[Images.ExpX1],
                        2 to images[Images.ExpX2],
                        3 to images[Images.ExpX3],
                        4 to images[Images.ExpX4]
                    ).entries.firstOrNull { (_, pattern) ->
                        countRegion.exists(pattern, similarity = 0.87)
                    }?.key

                    if (count == null || count > prefs.maxGoldEmberSetSize) {
                        continue
                    }
                }

                gift.region.click()
                ++clickCount
            }
        }

        return clickCount
    }
}
