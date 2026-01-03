package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class LotteryLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {
    val finishedRegion = Region(-510, 700, 55, 100).xFromCenter()
    val checkRegion = Region(-1130, 700, 675, 330).xFromCenter()
    val spinClick = Location(-646, 860).xFromCenter()
    val fullPresentBoxRegion = Region(20, 860, 1000, 500).xFromCenter()

    val doneRegion = when(gameServer) {
        is GameServer.Jp -> Region(-560, 700, 60, 90).xFromCenter()
        else -> Region(-510, 700, 60, 90).xFromCenter()
    }

    // when changing from 10 spin to 100 spin there would be a popup
    val transitionRegion = Region(1180, 30, 65, 70).xFromCenter()
}