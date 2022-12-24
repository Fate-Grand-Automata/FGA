package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import javax.inject.Inject

class LotteryLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {
    val finishedRegion = Region(-510, 700, 55, 100).xFromCenter()
    val checkRegion = Region(-1130, 700, 675, 330).xFromCenter()
    val spinClick = Location(-446, 860).xFromCenter()
    val fullPresentBoxRegion = Region(20, 860, 1000, 500).xFromCenter()
    val lineupUpdatedRegion = Region(-320, 360, 640, 100).xFromCenter()

    // center of screen
    val confirmNewLineupClick = Location(1280, 720)
}