package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.Duration

/**
 * CE bomb maker script with caveats.
 * This script isn't much tested. Use at your own risk.
 *
 * - Can only be started from CE enhancement screen with no CE selected.
 * - In the CE picking screens, the item sizes must be set to lowest.
 * - Base CE pickup screen should be filtered to correct rarity and sorted in Ascending order by Level.
 * - Enhancement material pickup screen should be filtered to correct rarity and sorted in Descending order by Level.
 */
class AutoCEBomb @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    sealed class ExitReason {
        object NoSuitableTargetCEFound: ExitReason()
    }

    private fun imagesForSelectedRarity() = when (prefs.ceBombTargetRarity) {
        1 -> listOf(
            Images.CEStarvationLv1,
            Images.CEAwakeningLv1,
            Images.CEBarrierLv1,
            Images.CELinkageLv1,
            Images.CECombatLv1
        )
        2 -> listOf(
            Images.CEGloomLv1,
            Images.CESynchronizationLv1,
            Images.CEDeceptionLv1,
            Images.CEProsperityLv1,
            Images.CEMercyLv1
        )
        else -> emptyList()
    }

    class ExitException(val reason: ExitReason): Exception()

    private fun findBaseCE(): Match {
        for (img in imagesForSelectedRarity()) {
            val matches = game.levelOneCERegion
                .findAll(images[img])
                .toList()
                .sorted()

            // At least 2 copies are needed to merge
            if (matches.size > 1) {
                return matches[0]
            }
        }

        throw ExitException(ExitReason.NoSuitableTargetCEFound)
    }

    override fun script(): Nothing {
        game.ceEnhanceClick.click()

        while (true) {
            Duration.seconds(2).wait()

            val baseCERegion = findBaseCE().Region
            val img = baseCERegion.getPattern()

            img.use {
                baseCERegion.click()
                Duration.seconds(2).wait()

                Location(900, 500).click()
                Duration.seconds(2).wait()

                pickMatchingCE(img)
                pickCEs()

                repeat(2) {
                    Location(2300, 1300).click()
                    Duration.seconds(1).wait()
                }

                Location(1600, 1200).click()
                Duration.seconds(1).wait()

                Location(2000, 1000).click(70)
                game.ceEnhanceClick.click()
            }
        }
    }

    private fun pickMatchingCE(img: IPattern) {
        val matchingCE = game.levelOneCERegion.find(img)
            ?: throw ExitException(ExitReason.NoSuitableTargetCEFound)

        matchingCE.Region.click()
        Duration.seconds(1).wait()
    }

    private fun pickCEs() {
        Location(2040, 1400).click()
        Duration.seconds(2).wait()

        for (y in 0..3) {
            for (x in 0..6) {
                Location(1900 - 270 * x, 1300 - 290 * y).click()
            }
        }
    }
}