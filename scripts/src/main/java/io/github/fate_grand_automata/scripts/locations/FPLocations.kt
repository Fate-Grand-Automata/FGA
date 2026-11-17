package io.github.fate_grand_automata.scripts.locations

import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FPLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {
    val summonCheck = Region(250, 1121, 100, 143).xFromCenter()

    val initialSummonCheck = Region(-305, 1121, 240, 183).xFromCenter()
    val initialSummonClick = Location(2, 977).xFromCenter()
    val initialSummonContinueClick = Region(341, 1145, 120, 60).xFromCenter()

    val continueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val first10SummonClick = Location(400, 1062).xFromCenter()

    val okClick = Location(320, 1120).xFromCenter()
    val continueSummonClick = Location(320, 1325).xFromCenter()
    val skipRapidClick = Location(1240, 1400).xFromCenter()
}