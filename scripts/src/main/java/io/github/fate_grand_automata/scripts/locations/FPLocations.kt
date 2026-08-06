package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FPLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {
    // 9th anniversary adds 100x summon to the FP screen
    private val afterAnni9 = gameServer is GameServer.Jp || gameServer is GameServer.Cn || gameServer is GameServer.Kr || gameServer is GameServer.En

    val summonCheck = if (afterAnni9)
        Region(250, 1121, 100, 143).xFromCenter()
    else
        Region(100, 1152, 75, 143).xFromCenter()

    val initialSummonCheck = Region(-305, 1121, 240, 183).xFromCenter()
    val initialSummonClick = if (afterAnni9)
        Location(2, 977).xFromCenter()
    else
        Location(2, 1052).xFromCenter()
    val initialSummonContinueClick = Region(341, 1145, 120, 60).xFromCenter()

    val continueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val first10SummonClick = if (afterAnni9)
        // the old location is still valid but only for 10x FP
        Location(400, 1062).xFromCenter()
    else
        Location(170, 1062).xFromCenter()

    val okClick = Location(320, 1120).xFromCenter()
    val continueSummonClick = Location(320, 1325).xFromCenter()
    val skipRapidClick = Location(1240, 1400).xFromCenter()
}