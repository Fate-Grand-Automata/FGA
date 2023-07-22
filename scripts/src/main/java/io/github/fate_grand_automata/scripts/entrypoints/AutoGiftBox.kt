package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.Swiper
import io.github.lib_automata.dagger.ScriptScope
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
        class CannotSelectAnyMore(val pickedStacks: Int, val pickedGoldEmbers: Int) : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    companion object {
        const val maxClickCount = 99
        const val maxNullStreak = 3
    }

    private data class IterationResult(
        val pickedStacks: Int,
        val pickedGoldEmbers: Int
    ) {
        operator fun plus(other: IterationResult): IterationResult {
            return IterationResult(
                pickedStacks + other.pickedStacks,
                pickedGoldEmbers + other.pickedGoldEmbers
            )
        }
    }

    override fun script(): Nothing {
        var totalSelected = IterationResult(0, 0)
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
            totalSelected += picked

            if (picked.pickedStacks > 0) {
                receiveSelectedClick.click()
                while (true) {
                    2.seconds.wait()
                    if (connectionRetry.needsToRetry()) connectionRetry.retry() else break
                }
                receiveSelectedClick.click()
            } else break

            if (receiveEnabledPattern !in receiveEnabledRegion) break
        }

        throw ExitException(
            ExitReason.CannotSelectAnyMore(
                totalSelected.pickedStacks,
                totalSelected.pickedGoldEmbers
            )
        )
    }

    private fun iteration(
        checkRegion: Region,
        scrollEndRegion: Region
    ): IterationResult {
        var iterationResult = IterationResult(0, 0)
        var aroundEnd = false
        var nullStreak = 0

        while (iterationResult.pickedStacks < maxClickCount &&
            iterationResult.pickedGoldEmbers < prefs.maxGoldEmberTotalCount
        ) {
            val picked = useSameSnapIn {
                if (!aroundEnd) {
                    // The scrollbar end position matches before completely at end
                    // a few items can be left off if we're not careful
                    aroundEnd = images[Images.GiftBoxScrollEnd] in scrollEndRegion
                }

                pickGifts(checkRegion)
            }

            iterationResult += picked

            swipe(
                locations.giftBoxSwipeStart,
                locations.giftBoxSwipeEnd
            )

            if (aroundEnd) {
                // Once we're around the end, stop after we don't pick anything consecutively
                if (picked.pickedStacks == 0) {
                    ++nullStreak
                } else nullStreak = 0

                if (nullStreak >= maxNullStreak) {
                    break
                }

                // Longer animations. At the end, items pulled up and released.
                1.seconds.wait()
            }
        }

        return iterationResult
    }

    // Return number of selected gold cards
    private fun pickGifts(checkRegion: Region): IterationResult {
        var clickCount = 0
        var selectedGoldCards = 0

        for (gift in checkRegion.findAll(images[Images.GiftBoxCheck]).sorted()) {
            val countRegion = when (prefs.gameServer) {
                is GameServer.Jp, GameServer.Tw, GameServer.Cn -> -1000
                is GameServer.En -> -830
                GameServer.Kr -> -1010
            }.let { x -> Region(x, -115, 300, 100) } + gift.region.location

            val iconRegion = Region(-1480, -116, 300, 240) + gift.region.location

            val gold = images[Images.GoldXP] in iconRegion
            val silver = !gold && images[Images.SilverXP] in iconRegion

            if (gold || silver) {
                if (gold) {
                    val text = countRegion.detectText(true)
                        // replace common OCR mistakes
                        .replace("%", "x")
                        .replace("S", "5")
                        .replace("O", "0")
                        .lowercase()
                    val regex = Regex("""x ?(\d+)$""")
                    // extract the count if it was found in the text
                    val count = regex.find(text)?.groupValues?.getOrNull(1)?.toInt()

                    if (count == null || count > prefs.maxGoldEmberStackSize) {
                        continue
                    } else {
                        selectedGoldCards += count
                    }
                }

                gift.region.click()
                clickCount++

                if (selectedGoldCards > prefs.maxGoldEmberTotalCount) {
                    break
                }
            }
        }

        return IterationResult(clickCount, selectedGoldCards)
    }
}
