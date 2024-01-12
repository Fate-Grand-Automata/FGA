package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.CEDisplayChangeAreaEnum
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
import io.github.lib_automata.Swiper
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds


@ScriptScope
class AutoCEBomb @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi,
    private val connectionRetry: ConnectionRetry,
    private val swiper: Swiper
) : EntryPoint(exitManager), IFgoAutomataApi by api {

    sealed class ExitReason {
        /**
         * No suitable target/fodder CE was found.
         */
        data object NoSuitableTargetCEFound : ExitReason()

        /**
         * The target CE is already max level.
         * This is done if the user have setup the initial target CE, and after continues runs
         * the target CE have reached max level.
         */
        data object TargetCEMaxLevel : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    private val fodderCeRows = 3
    private val targetCeRows = 4
    private val ceColumns = 7

    /**
     * List of rows to skip when looking for fodder CEs.
     * This is to save time on checking rows that have no CEs in them.
     *
     * A Row is added when a whole column of CEs are empty.
     * @see getClickLocationList
     */
    private var skipRow = mutableListOf<Int>()

    /**
     * List of columns to skip when looking for fodder CEs.
     * This is to save time on checking columns that have no CEs in them.
     *
     * @see getLockLocations
     */
    private var skipColumn = mutableListOf<Int>()

    private var firstTargetSetupDone = false
    private var firstFodderSetupDone = false

    private var initialCEEnhancementRun = true

    override fun script(): Nothing {
        loop()
    }

    fun loop(): Nothing {
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { isEmptyEnhance() } to { pickTarget() },
            { isFinalConfirmVisible() } to {
                runEnhancement()
            },
            { isInPickUpCraftEssenceScreen() } to {
                performCraftEssenceUpgrade()
            },
            { isInCraftEssenceEnhancementMenu() && !isEmptyEnhance() } to {
                checkIfCEisLockedAndClickEnhancementMenuLocation()
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

    /**
     * This will check if the target CE is not locked
     *
     * If it is not locked, it will lock it
     *
     * Afterwards, it will click on the CE Enhancement Menu
     * To pick the fodder CEs
     *
     * @see performCraftEssenceUpgrade
     *
     */
    private fun checkIfCEisLockedAndClickEnhancementMenuLocation() {
        checkIfCEisLocked()
        locations.ceBomb.ceOpenEnhancementMenuLocation.click()
    }

    /**
     * This can only be done when Android 8.0 and above
     *
     * If this is not the initial run of the CE, it will skip this to save time.
     *
     * This will check if the target CE is locked
     * So as to prevent it from being used as fodder when another CE run is started
     *
     * After checking and settings up the lock if needed, it will return to the CE Enhancement Menu
     */
    private fun checkIfCEisLocked() {
        if (!initialCEEnhancementRun) return

        if (prefs.craftEssence.skipAutoLockTargetCE) return

        locations.emptyEnhanceRegion.longPress()

        2.seconds.wait()

        if (!isSelectedCELocked()) {
            locations.ceBomb.selectedCELockedRegion.click()
            0.5.seconds.wait()
        }
        while (true) {
            locations.ceBomb.selectedCEBackButtonRegion.click()

            initialCEEnhancementRun = false

            val returnToMenu = waitUntilCraftEssenceEnhancementMenuExist()
            if (returnToMenu) break
        }
    }

    /**
     * Will perform the CE Enhancement
     *
     * It would press the "Ok" button
     * and then fire the initial rapid clicks on the screen
     * to speed up the enhancement process
     */
    private fun runEnhancement() {
        locations.ceBomb.cePerformEnhancementOkButton.click()
        0.5.seconds.wait()
        locations.ceBomb.enhancementSkipRapidClick.click(15)
    }

    /**
     * Will pick the target CE to upgrade.
     *
     * Also initializes the setup for the whole run until the next target CE is selected.
     *
     */
    private fun pickTarget() {
        if (!prefs.craftEssence.emptyEnhance){
            throw ExitException(ExitReason.TargetCEMaxLevel)
        }
        skipRow.clear()
        skipColumn.clear()

        initialCEEnhancementRun = true


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

    /**
     * Will perform the CE Upgrade
     *
     * - It would always fix the position of the CE
     * @see fixPosition
     * - If it is the first run of the CE, it would perform the initial setup
     * otherwise, it would skip this to save time
     * @see initialScreenSetup
     * @see setFodderCEFilters
     *
     * - It would pick the fodder CEs
     * @see pickFodderCEs
     *
     * - It would click on the upgrade button
     *
     * - It would check if the CE details still exist that would mean that there
     * were no Fodder CEs to use for upgrading
     * @see ExitReason.NoSuitableTargetCEFound
     *
     * - If it doesn't exist, it would then click the Enhance button
     * and then let the "Ok" Button be handled by the main loop
     * @see isFinalConfirmVisible
     */
    private fun performCraftEssenceUpgrade() {
        fixPosition()

        if (!firstFodderSetupDone) {
            initialScreenSetup()
            setFodderCEFilters()

            firstFodderSetupDone = true
        }

        pickFodderCEs()

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

    /**
     * Ensures that the CE's position is on the top of the list
     */
    private fun fixPosition() {
        locations.ceBomb.ceScrollbar.click(3)

        val startSwipeFixLocation = CELocation(0, 0)
        val endSwipeFixLocation = CELocation(0, 1)

        swiper(
            start = startSwipeFixLocation,
            end = endSwipeFixLocation,
        )
        0.5.seconds.wait()

        locations.ceBomb.ceScrollbar.click(3)
    }

    /**
     * Will perform initial setup for the CE Bomb, such as changing the display size,
     * setting the target CE filters, and setting the fodder CE filters
     *
     * This will only run once per CE.
     *
     * Will reset the screen once selecting a new target CE in the same run.
     * @see pickTarget
     */
    private fun initialScreenSetup() {
        setDisplaySize()
        setupSortFeatures()
    }

    /**
     * It checks if the user has disabled the automatic display change
     * If so, it will skip this
     *
     * Otherwise, it will click on the display change button
     * and click on the user's selected display change area
     *
     * The location of the play button is being check on
     * [io.github.fate_grand_automata.scripts.modules.AutoSetup.checkIfCanAutomaticDisplayChangeInCE]
     *
     * If the display is already small, it will skip this
     *
     * If the display is not small, it will click on the user's selected display change area
     * until the display is small
     *
     * If the display is still not small after 10 clicks, it will skip this
     * and just let the user handle it manually when it has thrown an error
     * because the CEs are not visible.
     * @see ExitReason.NoSuitableTargetCEFound
     */
    private fun setDisplaySize() {
        if (prefs.craftEssence.skipAutomaticDisplayChange) return
        2.seconds.wait()

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
            var displayLoop = 0
            while (!isDisplaySmall()) {
                displayLocation.click()
                1.seconds.wait()
                displayLoop++
                if (displayLoop >= 10) {
                    break
                }
            }
        }
    }

    /**
     * Will set the target CE filters
     *
     * This will only run once per CE.
     *
     * Will reset the screen once selecting a new target CE in the same run.
     *
     * Will skip if the user has disabled the filter feature detection
     *
     */
    private fun setTargetCEFilters() {
        if (prefs.craftEssence.skipCEFilterDetection) return
        2.seconds.wait()

        locations.ceBomb.changeFilterButtonLocation.click()
        2.seconds.wait()

        useSameSnapIn {
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
        }
        var retry = 5
        while (true){
            locations.ceBomb.filterCloseLocation.click()
            val didFiveStarVanish = locations.ceBomb.filterByRarityRegion(rarity = 5).waitVanish(
                image = images[Images.CraftEssenceFodderCEFilterOff],
                timeout = 2.seconds
            )
            if (didFiveStarVanish) break
            if (retry == 0){
                throw ExitException(ExitReason.NoSuitableTargetCEFound)
            }
            retry--
        }
        0.5.seconds.wait()
    }

    /**
     * Will set the fodder CE filters
     *
     * This will only run once per CE.
     *
     * Will reset the screen once selecting a new target CE in the same run.
     *
     * Will skip if the user has disabled the filter feature detection
     *
     */
    private fun setFodderCEFilters() {
        if (prefs.craftEssence.skipCEFilterDetection) return
        2.seconds.wait()

        locations.ceBomb.changeFilterButtonLocation.click()
        2.seconds.wait()

        useSameSnapIn {
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
        }
        var retry = 5
        while (true){
            locations.ceBomb.filterCloseLocation.click()
            val didFiveStarVanish = locations.ceBomb.filterByRarityRegion(rarity = 5).waitVanish(
                image = images[Images.CraftEssenceFodderCEFilterOff],
                timeout = 2.seconds
            )
            if (didFiveStarVanish) break
            if (retry == 0){
                throw ExitException(ExitReason.NoSuitableTargetCEFound)
            }
            retry--
        }
        0.5.seconds.wait()
    }

    /**
     * Will setup the sort features for the CE Bomb
     *
     * This will only run once per CE.
     *
     * Will reset the screen once selecting a new target CE in the same run.
     * @see pickTarget
     *
     * Will skip if the user has disabled the sort feature detection
     *
     * The following will be enabled depending on the user's settings:
     * - Smart Sort
     * - Select Sort
     * - Sort by Level
     *
     * Afterwards, it will close the sort menu
     *
     */
    private fun setupSortFeatures() {
        if (prefs.craftEssence.skipSortDetection) return
        2.seconds.wait()

        locations.ceBomb.sortButtonLocation.click()
        2.seconds.wait()

        useSameSnapIn {
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
        }

        locations.ceBomb.sortCloseLocation.click()
        2.seconds.wait()

    }

    /**
     * Will pick the target CE to upgrade.
     *
     * Will click on the position of every 28 possible CE on the screen
     * until one was selected to be upgraded or none worked
     *
     * If there was a CE selected, it would return to the craft essence enhancement menu
     * and this is check by checking if the CE details exist
     * @see Images.CEDetails
     *
     * @throws ExitException if no suitable target CE was found
     * @see ExitReason.NoSuitableTargetCEFound
     */
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

    /**
     * Will pick the fodder CEs to use for upgrading the target CE
     *
     * If dragging is enabled, it would perform long press and drag
     * @see longPressAndDragOrMultipleClicks
     *
     * Otherwise, it would perform multiple clicks
     * which is suitable for Android 7.1 and below
     *
     */
    private fun pickFodderCEs() {
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
     *
     * It would get the locations of all the CEs on the screen with locks first
     * @see getLockLocations
     *
     * In order to skip those locations when checking for CEs to click using
     * @see getClickLocationList
     *
     * If there are no CEs with stars, it would exit
     * @see ExitReason.NoSuitableTargetCEFound
     *
     * After that, it would check if there is only one row with stars and if it has less than 4 CEs
     * If so, it would perform multiple clicks on those CEs
     *
     * Otherwise, it would perform long press and drag on the CEs
     * @see longPressAndSwipe
     */

    private fun longPressAndDragOrMultipleClicks() {
        getLockLocations()

        val clicksArray = getClickLocationList()

        if (clicksArray.isEmpty()) throw ExitException(ExitReason.NoSuitableTargetCEFound)

        when {
            clicksArray.size == 1 && clicksArray.first().size < 4 -> clicksArray.first().forEach { it.click() }
            else -> longPressAndSwipe(clicksArray, chunked = ceColumns)
        }
        0.5.seconds.wait()
    }

    /**
     * This will get the locations of all the CEs on the screen
     * By checking for the star on the CE
     *
     * This will start from the bottom right and go up the rows
     *
     * If a star is found, it will add the location to the clicksColumns list
     *
     * If a column is empty, it will add the row to the skipRow list
     * so that it would not check that row again during the next iteration.
     * @see skipRow
     *
     * Otherwise, it would reverse the list so that it would start from left to right in that row.
     * And then, it would add the clicksColumns list to the clicksRows list
     *
     * Before returning the clicksRows list, it would reverse it so that it would start from the top left
     *
     * @return a list of lists of locations to click
     */
    private fun getClickLocationList(): List<List<Location>> {
        val clicksRows = mutableListOf<List<Location>>()

        useSameSnapIn {
            var foundCraftEssence = false
            for (y in fodderCeRows downTo 0) {
                // skip rows that have no CE in them to save time on checking them again
                if (y in skipRow) continue

                val clicksColumns = mutableListOf<Location>()
                for (x in (ceColumns - 1) downTo 0) {
                    // lock exists, skip specific location
                    if (x + (y * 7) in skipColumn) continue

                    if (foundCraftEssence || doesCraftEssenceExist(x, y)) {
                        clicksColumns.add(CELocation(x, y))
                        foundCraftEssence = true
                    }
                }
                if (clicksColumns.isEmpty()) {
                    skipRow.add(y)
                } else {
                    clicksRows.add(clicksColumns.reversed())
                }
            }
        }

        return clicksRows.reversed()
    }

    /**
     * Will get the locations of all the locks on the screen
     * and add them to the skipColumn list
     *
     * This will start from the top left and go down the rows
     *
     * if all CEs are locked, it will exit
     * @see ExitReason.NoSuitableTargetCEFound
     *
     * @see skipColumn
     */
    private fun getLockLocations() {
        var foundLock = true

        useSameSnapIn {
            loop@ for (y in 0 until fodderCeRows) {
                // skip rows that have no CE in them to save time on checking them again
                if (y in skipRow) continue

                for (x in 0 until ceColumns) {
                    val index = x + (y * 7)
                    if (index in skipColumn) continue

                    if (!foundLock) break@loop

                    if (doesLockExist(x, y)) {
                        skipColumn.add(index)
                    } else {
                        foundLock = false
                    }
                }
            }
        }


        // if all are lock, exit
        if (foundLock) throw ExitException(ExitReason.NoSuitableTargetCEFound)
    }

    private fun isEmptyEnhance() = images[Images.EmptyEnhance] in locations.emptyEnhanceRegion

    /**
     * CE Banner on only appears when there is a CE selected
     */
    private fun isInCraftEssenceEnhancementMenu() = images[Images.CraftEssenceBannerOn] in
            locations.ceBomb.ceBannerOnRegion

    private fun waitUntilCraftEssenceEnhancementMenuExist() = locations.ceBomb.ceBannerOnRegion.exists(
        image = images[Images.CraftEssenceBannerOn],
        timeout = 30.seconds
    )

    /**
     * Checks for the star in the CE selection screen.
     */
    private fun isInPickUpCraftEssenceScreen() = images[Images.CEDetails] in
            locations.ceBomb.ceMultiSelectRegion

    /**
     * From last testing, the similarity already achieves more than 80% similarity.
     * But just to be safe, it was set to 60%.
     */
    private fun doesCraftEssenceExist(x: Int, y: Int) =
        locations.ceBomb.craftEssenceStarRegion(x, y).exists(images[Images.CraftEssenceStar], similarity = 0.60)

    /**
     * From last testing, the similarity already achieves more than 80% similarity.
     * But just to be safe, it was set to 60%.
     */
    private fun doesLockExist(x: Int, y: Int) =
        locations.ceBomb.craftEssenceLockRegion(x, y).exists(images[Images.CraftEssenceLock], similarity = 0.60)


    private fun CELocation(x: Int, y: Int) = locations.ceBomb.ceFirstFodderLocation +
            Location(x * 270, y * 290 + 50)

    /**
     * This checks the gap between 2 CEs. If the CE display is large it won't be seen.
     */
    private fun isDisplaySmall() = images[Images.CraftEssenceDisplaySmall] in
            locations.ceBomb.displaySizeCheckRegion

    private fun isSmartSortOn() = images[Images.CraftEssenceOn] in locations.ceBomb.smartSortRegion

    private fun isSelectSortOn() = images[Images.CraftEssenceOn] in locations.ceBomb.selectSortRegion

    private fun isSortByLevelOff() = images[Images.CraftEssenceFodderCEFilterOff] in
            locations.ceBomb.sortByLevelRegion

    private fun isFinalConfirmVisible() = images[Images.Ok] in
            locations.ceBomb.getFinalConfirmRegion

    /**
     * Will check if the Target CE is locked
     * Can only be done when Android 8.0 and above
     */
    private fun isSelectedCELocked() = images[Images.CraftEssenceFodderSelectedCELocked] in
            locations.ceBomb.selectedCELockedRegion
}