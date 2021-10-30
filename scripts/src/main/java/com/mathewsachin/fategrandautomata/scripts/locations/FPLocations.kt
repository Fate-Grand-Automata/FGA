package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FPLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
): IScriptAreaTransforms by scriptAreaTransforms {
    val summonCheck = Region(100, 1152, 75, 143).xFromCenter()
    val continueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val first10SummonClick = Location(120, 1062).xFromCenter()
    val okClick = Location(320, 1120).xFromCenter()
    val continueSummonClick = Location(320, 1325).xFromCenter()
    val skipRapidClick = Location(1240, 1400).xFromCenter()
}