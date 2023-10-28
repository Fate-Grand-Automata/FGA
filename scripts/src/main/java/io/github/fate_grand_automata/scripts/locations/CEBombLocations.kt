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
    val displayCheckRegion = when (isWide) {
        true -> Region(614, 610, 30, 22)
        false -> Region(382, 611, 30, 22)
    }

    /***
     * Added two locations for the display change button click
     */
    val displayChangeLocationTopRight = when (isWide) {
        true -> Location(301, 1206)
        false -> Location(118, 1288)
    }

    val displayChangeLocationBottomLeft = when (isWide) {
        true -> Location(182, 1326)
        false -> Location(5, 1411)
    }

    val filtersLocation = Location(1953, 255) + Location(if (isWide) 265 else 0, 0)

    fun filters5StarRegion(rarity: Int) = when (isWide) {
        true ->  Region(709, 408, 41, 36)
        false ->  Region(491, 408, 41, 36)
    } + Location((5 - rarity) * 374, 0)

    fun filters5StarLocation(rarity: Int) = when (isWide) {
        true -> Location(729, 464)
        false -> Location(511, 464)
    } + Location((5 - rarity) * 375, 0)

    val filterCloseLocation = Location(2109, 1269) + Location(if (isWide) 159 else 0, 0)
}