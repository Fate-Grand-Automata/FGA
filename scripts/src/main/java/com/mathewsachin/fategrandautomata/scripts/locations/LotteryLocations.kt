package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import javax.inject.Inject

class LotteryLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
): IScriptAreaTransforms by scriptAreaTransforms {
    val finishedRegion = Region(-780, 860, 180, 100).xFromCenter()
    val checkRegion = Region(-1130, 800, 340, 230).xFromCenter()
    val spinClick = Location(-446, 860).xFromCenter()
    val fullPresentBoxRegion = Region(20, 860, 1000, 500).xFromCenter()
    val resetClick = Location(if (isWide) 1160 else 920, 480).xFromCenter()
    val resetConfirmationClick = Location(494, 1122).xFromCenter()
    val resetCloseClick = Location(-10, 1120).xFromCenter()
}