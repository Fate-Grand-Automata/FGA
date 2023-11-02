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
    var count = 0

    var firstTargetSetupDone = false
    var firstFodderSetupDone = false

    override fun script(): Nothing {
        skipRow.clear()
        loop()
    }

    fun loop(): Nothing {
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { isEmptyEnhance() } to { pickTarget() },
            { isFinalConfirmVisible() } to {
                locations.ceBomb.cePerformEnhancementOkButton.click()
            },
            { isInPickUpCraftEssenceScreen() } to {
                performCraftEssenceUpgrade()
            },
            { isInCraftEssenceEnhancementMenu() && !isEmptyEnhance() } to {
                locations.ceBomb.ceOpenEnhancementMenuLocation.click()
            },
        )

        while (true) {
            val actor = useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            } ?: { locations.ceBomb.enhancementSkipRapidClick.click(5) }
            actor.invoke()

            0.5.seconds.wait()
        }
    }

    private fun pickTarget() {
        locations.ceBomb.ceSelectCEToEnhanceLocation.click()

        // waits until CE details Exist
        val found = locations.ceBomb.ceMultiSelectRegion.exists(
            image = images[Images.CEDetails],
            timeout = 2.seconds
        )

        if (!found) return

        if (!firstTargetSetupDone) {
            initialScreenSetup()
            setTargetCEFilters()
            firstTargetSetupDone = true
        }

        pickTargetCraftEssenceToUpgrade()
    }

    private fun performCraftEssenceUpgrade() {
        if (!firstFodderSetupDone) {
            initialScreenSetup()
            setFodderCEFilters()

            firstFodderSetupDone = true
        }


        // ensure to be on top of the list
        locations.ceBomb.ceScrollbar.click(if (count == 0) 3 else 1)

        useDraggingOrNotToPickFodderCEs()

        locations.ceBomb.ceUpgradeOkButton.click()

        val exiting = locations.ceBomb.ceMultiSelectRegion.waitVanish(
            image = images[Images.CEDetails],
            timeout = 3.seconds
        )
        if (!exiting) {
            throw ExitException(ExitReason.NoSuitableTargetCEFound)
        }
        0.5.seconds.wait()
        locations.ceBomb.ceUpgradeOkButton.click()
    }

    private fun initialScreenSetup() {
        setDisplaySize()
        setupSortFeatures()
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

    private fun setupSortFeatures() {
        if (prefs.craftEssence.skipSortDetection) return

        locations.ceBomb.sortButtonLocation.click()
        2.seconds.wait()

        if (!isSmartSortOn()) {
            locations.ceBomb.smartSortRegion.center.click()
            0.5.seconds.wait()
        }

        if (!isSelectSortOn()) {
            locations.ceBomb.selectSortRegion.center.click()
            0.5.seconds.wait()
        }

        if (isSortByLevelOff()) {
            locations.ceBomb.sortByLevelRegion.center.click()
            0.5.seconds.wait()
        }

        locations.ceBomb.sortCloseLocation.click()
        2.seconds.wait()

    }

    private fun pickTargetCraftEssenceToUpgrade() {
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

    private fun useDraggingOrNotToPickFodderCEs() {
        if (prefs.craftEssence.useDragging) {
            longPressAndDragOrMultipleClicks()
        } else {
            for (y in 0 until fodderCeRows) {
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
        val clicksArray = getClickLocationList().reversed()

        if (clicksArray.isEmpty()) throw ExitException(ExitReason.NoSuitableTargetCEFound)

        when {
            clicksArray.size < 4 -> clicksArray.forEach { it.click() }
            else -> longPressAndSwipeOrMultipleClicks(clicksArray, chunked = ceColumns)
        }
        0.5.seconds.wait()
    }


    private fun getClickLocationList(): List<Location> {
        val clicksArray = mutableListOf<Location>()
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
            if (ceFound == 0) {
                skipRow.add(y)
            }
        }
        return clicksArray
    }

    private fun isEmptyEnhance() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    private fun isInCraftEssenceEnhancementMenu() = images[Images.CraftEssenceEnhancement] in
            locations.getCeEnhanceRegion(prefs.gameServer)

    private fun isInPickUpCraftEssenceScreen() = images[Images.CEDetails] in
            locations.ceBomb.ceMultiSelectRegion

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

    private fun isFinalConfirmVisible() = images[Images.Ok] in
            locations.ceBomb.getFinalConfirmRegion

    sealed class ExitReason {
        data object NoSuitableTargetCEFound : ExitReason()
        data object CEFullyUpgraded : ExitReason()
        data object MaxNumberOfIterations : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()
}