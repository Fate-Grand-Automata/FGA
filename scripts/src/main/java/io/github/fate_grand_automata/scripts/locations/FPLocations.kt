package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FPLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
): IScriptAreaTransforms by scriptAreaTransforms {
    val summonCheck = when (gameServer) {
        is GameServer.Jp -> Region(250, 1121, 100, 143).xFromCenter()
        else -> Region(100, 1152, 75, 143).xFromCenter()
    }

    val initialSummonCheck = Region(-265, 1131, 75, 143).xFromCenter()
    val initialSummonClick = Location(2, 1052).xFromCenter()

    // temporary need to be separate from initialSummonCheck due to pop-up
    // warning about the next summon being 100x
    // it only happens once though
    val initial100SummonCheck = Region(-225, 1121, 100, 143).xFromCenter()
    val initial100SummonClick = Location(400, 1052).xFromCenter()

    val continueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val first10SummonClick = Location(120, 1062).xFromCenter()
    val okClick = Location(320, 1120).xFromCenter()
    val continueSummonClick = Location(320, 1325).xFromCenter()
    val skipRapidClick = Location(1240, 1400).xFromCenter()
}