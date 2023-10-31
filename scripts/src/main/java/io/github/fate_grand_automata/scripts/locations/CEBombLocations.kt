package io.github.fate_grand_automata.scripts.locations

import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class CEBombLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    // click on the center of previous region
    val ceSelectCEToEnhanceLocation =
        if (isWide)
            Location(-900, 800).xFromCenter()
        else
            Location(400, 500)

    // The 20 CE grid located on the right of the selected CE to enhance
    // should be 20 empty dark gray "+" rectangles if no ce selected
    val ceOpenEnhancementMenuLocation =
        if (isWide)
            Location(200, 500).xFromCenter()
        else
            Location(900, 500)

    // on the CE selection screen, should be the top left CE
    val ceFirstFodderLocation =
        if (isWide)
            Location(-980, 450).xFromCenter()
        else
            Location(280, 430)

    // Ok button on the CE selection list
    val ceUpgradeOkButton =
        if (isWide)
            Location(-400, 1300).xFromRight()
        else
            Location(2300, 1300)

    // Ok button on the pop-up to ask if you want to use selected CEs to enhance
    val cePerformEnhancementOkButton =
        if (isWide)
            Location(450, 1200).xFromCenter()
        else
            Location(1600, 1200)

    // the "Multi Select" button on the CE Selection screen
    val ceMultiSelectRegion =
        if (isWide)
            Region(175, 880, 135, 115)
        else
            Region(0, 880, 135, 115)

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
        true -> Region(-932, 574, 28, 30).xFromCenter()
        false -> Region(-948, 574, 29, 29).xFromCenter()
    }

    fun craftEssenceStarRegion(x: Int, y: Int) = firstCEStarRegion + Location(x * 266, y * 285)

    val filtersLocation = when (isWide) {
        true -> Location(-776, 257).xFromRight()
        false -> Location(673, 257).xFromCenter()
    }

    fun filters5StarRegion(rarity: Int) = Region(-789, 408, 41, 36).xFromCenter() +
            Location((5 - rarity) * 374, 0)

    fun filters5StarLocation(rarity: Int) = Location(-767, 424).xFromCenter() +
            Location((5 - rarity) * 375, 0)

    val filterCloseLocation = when (isWide) {
        true -> Location(-668, 1270).xFromRight()
        false -> Location(829, 1269).xFromCenter()
    }
}