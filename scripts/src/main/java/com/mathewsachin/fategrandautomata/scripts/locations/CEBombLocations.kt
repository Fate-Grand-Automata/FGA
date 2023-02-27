package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import javax.inject.Inject

class CEBombLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {
    // the dark gray " + Tap to select a Craft Essence to Enhance" area
    val ceToEnhanceRegion =
        if (isWide)
            Region(-1100, 600, 400, 400).xFromCenter()
        else
            Region(200, 600, 400, 400)

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
}