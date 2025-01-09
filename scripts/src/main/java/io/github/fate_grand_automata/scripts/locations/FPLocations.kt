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

    val initialSummonCheck = Region(-305, 1121, 240, 183).xFromCenter()
    val initialSummonClick = when (gameServer) {
        is GameServer.Jp -> Location(2, 977).xFromCenter()
        else -> Location(2, 1052).xFromCenter()
    }

    val continueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val first10SummonClick = when (gameServer) {
        // the old location is still valid but only for 10x FP
        is GameServer.Jp -> Location(400, 1062).xFromCenter()
        else -> Location(120, 1062).xFromCenter()
    }
    val okClick = Location(320, 1120).xFromCenter()
    val continueSummonClick = Location(320, 1325).xFromCenter()
    val skipRapidClick = Location(1240, 1400).xFromCenter()
}