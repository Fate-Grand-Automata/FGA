package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * CE bomb maker script with caveats.
 * This script isn't much tested. Use at your own risk. This code isn't calibrated for FGO JP.
 *
 * - Can only be started from CE enhancement screen with no CE selected.
 * - In the CE picking screens, the item sizes must be set to lowest.
 * - It will select the top left CE to upgrade it
 */
@ScriptScope
class AutoCEBomb @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi,
    private val connectionRetry: ConnectionRetry
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    override fun script(): Nothing {

        // Click on the "Tap to select a Craft Essence to Enhance" area
        locations.ceBomb.ceSelectCEToEnhanceLocation.click()
        2.seconds.wait()

        // Pick the first possible CE of the list
        // going from top left to bottom right
        pickCEToUpgrade()
        2.seconds.wait()

        locations.ceBomb.ceOpenEnhancementMenuLocation.click()
        2.seconds.wait()

        /** Loop to upgrade the CE selected
         *  The exit conditions are either:
         *  - CE is fully leveled
         *  - An iteration went through without having selected a enhance fodder
         *  - Too many iterations passed (just in case something went wrong)
         *
         *  Start condition: a CE is selected for enhancement
         *  the list of Craft Essence to feed to it is shown
         *
         *  It will:
         *  - select up to 20 enhance fodder
         *  - Press on ok
         *  - Press on enhance
         *  - Press on ok on the "Perform enhancement" pop up window
         *  - try to come back to the list of enhance fodder
         **/
        var count = 0
        while (true) {

            // Check for possible connection retry
            if (connectionRetry.needsToRetry()) {
                connectionRetry.retry()
            }

            count++

            // A CE to enhance is selected, now to select the 20 CE to feed to it
            pickCEEnhanceFodder()

            // Press Ok to exit the "Please select a craft essence to use for enhancement screen
            // Press Ok again to enhance
            repeat(2) {
                locations.ceBomb.ceUpgradeOkButton.click()
                1.seconds.wait()
            }

            // if at that point we're still on the CE selection page
            // Then that means no CE was selected, so we exit the script
            if (images[Images.CEDetails] in locations.ceBomb.ceMultiSelectRegion) {
                throw ExitException(ExitReason.NoSuitableTargetCEFound)
            }

            // Enhancement confirmation window is now up
            // press Ok to enhance
            locations.ceBomb.cePerformEnhancementOkButton.click()
            2.seconds.wait()

            /**
             * Enhancement animation goes on
             * loop until we're back to the CE
             * or encountered an exit condition
             */
            var subcount = 0
            while (images[Images.CEDetails] !in locations.ceBomb.ceMultiSelectRegion) {

                /** End the script if a CE is fully upgraded
                 * The CE would have been removed from the ceToEnhance region
                 * and we'd be back to a "Tap to select a Craft Essence to Enhance" state
                 **/
                if (images[Images.EmptyEnhance] in locations.ceBomb.ceToEnhanceRegion) {
                    throw ExitException(ExitReason.CEFullyUpgraded)
                }

                /**
                 * End of the script if we're stuck in this loop for some reason over 200 times
                 */
                if (subcount > 200) {
                    throw ExitException(ExitReason.MaxNumberOfIterations)
                }

                // Check for possible connection retry
                if (connectionRetry.needsToRetry()) {
                    connectionRetry.retry()
                }

                // click on the location of the 20 CE grid
                locations.ceBomb.ceOpenEnhancementMenuLocation.click()
                subcount++
            }

            /** As we can only have up to 600 CE at once
             * we can stop if we tried to feed 24CE, 40 times
             * That way the script at least ends after ~24 minutes
             * if it ran out of CE fodder and didn't detect it for some reason
             **/
            if (count > 40) {
                throw ExitException(ExitReason.MaxNumberOfIterations)
            }
        }
    }

    private fun pickCEToUpgrade() {
        /**
         * Will click on the position of every 28 possible CE on the screen
         * until one was selected to be upgraded or none worked
         */
        var y = 0
        var x = 0
        while (images[Images.CEDetails] in locations.ceBomb.ceMultiSelectRegion) {
            val currentLocation = Location(
                locations.ceBomb.ceFirstFodderLocation.x + (x * 270),
                locations.ceBomb.ceFirstFodderLocation.y + (y * 290) + 50
            )
            currentLocation.click()

            if (x < 6) {
                x++
            } else if (y < 3) {
                y++
                x = 0
            } else {
                throw ExitException(ExitReason.NoSuitableTargetCEFound)
            }
        }
    }

    private fun pickCEEnhanceFodder() {
        /**
         * Will click on the position of every 28 possible CE on the screen
         * to attempt to feed them to the CE bomb
         */
        for (y in 0..3) {
            for (x in 0..6) {
                val nextCELocation = Location(
                    locations.ceBomb.ceFirstFodderLocation.x + (x * 270),
                    locations.ceBomb.ceFirstFodderLocation.y + (y * 290) + 50
                )
                nextCELocation.click()
            }
        }
    }

    sealed class ExitReason {
        object NoSuitableTargetCEFound : ExitReason()
        object CEFullyUpgraded : ExitReason()
        object MaxNumberOfIterations : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()
}