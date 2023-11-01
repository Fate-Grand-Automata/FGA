package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.CEDisplayChangeAreaEnum
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
import io.github.lib_automata.LongPressAndSwipeOrMultipleClicks
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
    private val connectionRetry: ConnectionRetry,
    private val longPressAndSwipeOrMultipleClicks: LongPressAndSwipeOrMultipleClicks
) : EntryPoint(exitManager), IFgoAutomataApi by api {
    private val fodderCeRows = 3
    private val targetCeRows = 4
    private val ceColumns = 7

    var skipRow = mutableListOf<Int>()

    override fun script(): Nothing {

        skipRow.clear()

        if (prefs.craftEssence.emptyEnhance) {
            // Click on the "Tap to select a Craft Essence to Enhance" area
            locations.ceBomb.ceSelectCEToEnhanceLocation.click()
            2.seconds.wait()
            setTargetCEFilters()
            setupSortFeatures()

            setDisplaySize()
            // Pick the first possible CE of the list
            // going from top left to bottom right
            pickCEToUpgrade()
            2.seconds.wait()
        }

        locations.ceBomb.ceOpenEnhancementMenuLocation.click()
        2.seconds.wait()

        setFodderCEFilters()

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

            if (count == 0) {
                setDisplaySize()
                setupSortFeatures()
            }

            count++


            // A CE to enhance is selected, now to select the 20 CE to feed to it
            useDraggingOrNot()

            // Press Ok to exit the "Please select a craft essence to use for enhancement screen
            // Press Ok again to enhance
            repeat(2) {
                locations.ceBomb.ceUpgradeOkButton.click()
                1.seconds.wait()
            }

            /**
             * If at that point we're still on the CE selection page
             * then that means no CE was selected, so we exit the script
             **/
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
                if (images[Images.EmptyEnhance] in locations.emptyEnhanceRegion) {
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

    private fun setDisplaySize() {
        if (prefs.craftEssence.skipAutomaticDisplayChange) return

        val displayArea = prefs.craftEssence.ceDisplayChangeArea

        val displayRegion = locations.ceBomb.displayChangeRegion

        val topLeft = Location(displayRegion.x, displayRegion.y)
        val topRight = Location(displayRegion.right, displayRegion.y)

        val bottomLeft = Location(displayRegion.x, displayRegion.bottom)
        val bottomRight = Location(displayRegion.right, displayRegion.bottom)


        val displayLocation = when {
            CEDisplayChangeAreaEnum.TOP_RIGHT in displayArea -> topRight
            CEDisplayChangeAreaEnum.TOP_LEFT in displayArea -> topLeft
            CEDisplayChangeAreaEnum.BOTTOM_LEFT in displayArea -> bottomLeft
            CEDisplayChangeAreaEnum.BOTTOM_RIGHT in displayArea -> bottomRight
            else -> return
        }

        if (!isDisplaySmall()) {
            while (!isDisplaySmall()) {
                displayLocation.click()
                1.seconds.wait()
            }
        }
    }

    private fun setTargetCEFilters() {
        if (prefs.craftEssence.skipCEFilterDetection) return

        locations.ceBomb.changeFilterButtonLocation.click()
        2.seconds.wait()

        for (rarity in 5 downTo 1) {
            val filterStarRegion = locations.ceBomb.filterByRarityRegion(rarity = rarity)
            val filterLocation = locations.ceBomb.filterByRarityLocation(rarity = rarity)

            val filterOff = images[Images.CraftEssenceFodderCEFilterOff] in filterStarRegion

            if (rarity == prefs.craftEssence.ceTargetRarity && filterOff) {
                filterLocation.click()
                0.5.seconds.wait()
            }
            if (rarity != prefs.craftEssence.ceTargetRarity && !filterOff) {
                filterLocation.click()
                0.5.seconds.wait()
            }
        }
        if (isFilterClosable()) {
            locations.ceBomb.filterCloseLocation.click()
            1.seconds.wait()
        } else {
            throw ExitException(ExitReason.NoSuitableTargetCEFound)
        }
    }

    private fun setFodderCEFilters() {
        if (prefs.craftEssence.skipCEFilterDetection) return

        locations.ceBomb.changeFilterButtonLocation.click()
        2.seconds.wait()

        for (rarity in 5 downTo 1) {
            val filterStarRegion = locations.ceBomb.filterByRarityRegion(rarity = rarity)
            val filterLocation = locations.ceBomb.filterByRarityLocation(rarity = rarity)

            val filterOff = images[Images.CraftEssenceFodderCEFilterOff] in filterStarRegion

            if (rarity in prefs.craftEssence.ceFodderRarity && filterOff) {
                filterLocation.click()
                0.5.seconds.wait()
            }
            if (rarity !in prefs.craftEssence.ceFodderRarity && !filterOff) {
                filterLocation.click()
                0.5.seconds.wait()
            }
        }
        if (isFilterClosable()) {
            locations.ceBomb.filterCloseLocation.click()
            1.seconds.wait()
        } else {
            throw ExitException(ExitReason.NoSuitableTargetCEFound)
        }

    }

    private fun setupSortFeatures(){
        if (prefs.craftEssence.skipSortDetection) return

        locations.ceBomb.sortButtonLocation.click()
        2.seconds.wait()

        if (!isSmartSortOn()){
            locations.ceBomb.smartSortRegion.center.click()
            0.5.seconds.wait()
        }

        if (!isSelectSortOn()){
            locations.ceBomb.selectSortRegion.center.click()
            0.5.seconds.wait()
        }

        if(isSortByLevelOff()){
            locations.ceBomb.sortByLevelRegion.center.click()
            0.5.seconds.wait()
        }

        locations.ceBomb.sortCloseLocation.click()
        2.seconds.wait()

    }

    private fun pickCEToUpgrade() {
        /**
         * Will click on the position of every 28 possible CE on the screen
         * until one was selected to be upgraded or none worked
         */
        for (y in 0 until targetCeRows) {
            for (x in 0 until ceColumns) {
                CELocation(x, y).click()

                if (images[Images.CEDetails] !in locations.ceBomb.ceMultiSelectRegion) {
                    return
                }
            }
        }

        throw ExitException(ExitReason.NoSuitableTargetCEFound)
    }

    private fun useDraggingOrNot() {
        if (prefs.craftEssence.useDragging) {
            longPressAndDragOrMultipleClicks()
        } else {
            for (y in 0 until targetCeRows) {
                for (x in 0 until ceColumns) {
                    CELocation(x, y).click()
                }
            }
        }
    }

    /**
     * Will perform long press and drag for Android 8.0 and above,
     * otherwise it will perform multiple clicks
     */

    private fun longPressAndDragOrMultipleClicks() {
        val clicksArray = mutableListOf<Location>()

        /**
         * TODO: Add checking of the scroll button
         *    for when there is only few CEs left to make
         *    the checker faster
         */


        var foundCraftEssence = false
        for (y in fodderCeRows downTo 0) {
            // skip rows that have no CE in them to save time on checking them again
            if (y in skipRow) continue

            var ceFound = 0
            for (x in (ceColumns - 1) downTo 0) {
                if (foundCraftEssence || doesCraftEssenceExist(x, y)) {
                    clicksArray.add(CELocation(x, y))
                    foundCraftEssence = true
                    ceFound += 1
                }
            }
            if (ceFound == 0){
                skipRow.add(y)
            }
        }

        if (clicksArray.isEmpty()) throw ExitException(ExitReason.NoSuitableTargetCEFound)

        when {
            clicksArray.size < 4 -> clicksArray.forEach { it.click() }
            else -> longPressAndSwipeOrMultipleClicks(clicksArray.reversed(), chunked = ceColumns)
        }

    }

    private fun doesCraftEssenceExist(x: Int, y: Int) =
        locations.ceBomb.craftEssenceStarRegion(x, y).exists(images[Images.CraftEssenceStar], similarity = 0.60)


    private fun CELocation(x: Int, y: Int) = locations.ceBomb.ceFirstFodderLocation +
            Location(x * 270, y * 290 + 50)

    private fun isDisplaySmall() = images[Images.CraftEssenceDisplaySmall] in
            locations.ceBomb.displaySizeCheckRegion

    private fun isFilterClosable() = when (prefs.gameServer) {
        is GameServer.En -> images[Images.Ok] in locations.ceBomb.filterCloseRegion

        // the JP text was smaller than on other buttons of the same text
        else -> {
            val region = locations.ceBomb.filterCloseRegion
            images[Images.Ok].resize(region.size * 0.5) in region
        }
    }

    private fun isSmartSortOn() = images[Images.On] in locations.ceBomb.smartSortRegion

    private fun isSelectSortOn() = images[Images.On] in locations.ceBomb.selectSortRegion

    private fun isSortByLevelOff() = images[Images.CraftEssenceFodderCEFilterOff] in
        locations.ceBomb.sortByLevelRegion

    sealed class ExitReason {
        data object NoSuitableTargetCEFound : ExitReason()
        data object CEFullyUpgraded : ExitReason()
        data object MaxNumberOfIterations : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()
}