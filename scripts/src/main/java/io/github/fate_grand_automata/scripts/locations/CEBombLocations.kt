package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class CEBombLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    // click on the center of previous region
    val ceSelectCEToEnhanceLocation = when (isWide) {
        true -> Location(-900, 800).xFromCenter()
        false -> Location(400, 500)
    }


    // The 20 CE grid located on the right of the selected CE to enhance
    // should be 20 empty dark gray "+" rectangles if no ce selected
    val ceOpenEnhancementMenuLocation = when (isWide) {
        true -> Location(200, 500).xFromCenter()
        false -> Location(900, 500)
    }

    // on the CE selection screen, should be the top left CE
    val ceFirstFodderLocation = when (isWide) {
        true -> Location(-980, 450).xFromCenter()
        false -> Location(280, 430)
    }

    // Ok button on the CE selection list
    val ceUpgradeOkButton = when (isWide) {
        true -> Location(-400, 1300).xFromRight()
        false -> Location(2300, 1300)
    }

    // Ok button on the pop-up to ask if you want to use selected CEs to enhance
    val cePerformEnhancementOkButton = when (isWide) {
        true -> Location(450, 1200).xFromCenter()
        false -> Location(1600, 1200)
    }

    // the "Multi Select" button on the CE Selection screen
    val ceMultiSelectRegion = when (isWide) {
        true -> Region(175, 880, 135, 115)
        false -> Region(0, 880, 135, 115)
    }

    /**
     * This is checking the space in between CEs
     */
    val displaySizeCheckRegion = when (isWide) {
        true -> Region(-883, 610, 29, 23).xFromCenter()
        false -> Region(-898, 611, 30, 22).xFromCenter()
    }

    val displayChangeRegion = when (isWide) {
        true -> Region(182, 1206, 118, 119)
        false -> Region(-1280, 1288, 118, 123).xFromCenter()
    }

    val firstCEStarRegion = when (isWide) {
        true -> Region(-932, 582, 28, 30).xFromCenter()
        false -> Region(-948, 582, 29, 21).xFromCenter()
    }

    fun craftEssenceStarRegion(x: Int, y: Int) = firstCEStarRegion + Location(x * 266, y * 283)

    val changeFilterButtonLocation = when (isWide) {
        true -> Location(-776, 257).xFromRight()
        false -> Location(673, 257).xFromCenter()
    }

    val firstFilterRegion = Region(-789, 408, 41, 36).xFromCenter()

    fun filterByRarityRegion(rarity: Int) = firstFilterRegion +
            Location((5 - rarity) * 374, 0)

    val firstFilterLocation = Location(-767, 424).xFromCenter()

    fun filterByRarityLocation(rarity: Int) = firstFilterLocation +
            Location((5 - rarity) * 375, 0)

    val filterCloseRegion = when (gameServer) {
        is GameServer.En -> Region(771, 1240, 120, 60).xFromCenter()
        else -> Region(781, 1244, 100, 52).xFromCenter()
    }

    val filterCloseLocation = when (isWide) {
        true -> Location(-668, 1270).xFromRight()
        false -> Location(829, 1269).xFromCenter()
    }

    val sortButtonLocation = Location(1015, 256).xFromCenter()

    val smartSortRegion = Region(-160, 924, 67, 42).xFromCenter()

    val selectSortRegion = Region(724, 924, 68, 42).xFromCenter()

    val sortCloseLocation = Location(425, 1267).xFromCenter()

    val sortByLevelRegion = Region(-789, 408, 41, 36).xFromCenter()
}