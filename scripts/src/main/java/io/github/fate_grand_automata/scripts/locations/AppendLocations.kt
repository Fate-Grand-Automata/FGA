package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class AppendLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    val getAppendBannerRegion = when (gameServer) {
        is GameServer.En -> when (isWide) {
            true -> Region(-690, 21, 150, 88).xFromRight()
            false -> Region(-520, 20, 150, 88).xFromRight()
        }
        // JP option
        else -> when (isWide) {
            true -> Region(-838, 38, 90, 70).xFromRight()
            false -> Region(-666, 36, 90, 71).xFromRight()
        }
    }

    fun lockLocations(index: Int) = when (isWide) {
        true -> Region(-364, 466, 58, 52).xFromCenter() + Location(x = (index * 356), y = 0)
        false -> Region(-365, 508, 59, 53).xFromCenter() + Location(x = (index * 355), y = 0)
    }

    val okRegion = when(gameServer){
        is GameServer.En -> when (isWide) {
            true -> Region(341, 1094, 120, 60).xFromCenter()
            false -> Region(342, 1096, 121, 60).xFromCenter()
        }
        // JP Option
        else -> when (isWide) {
            true -> Region(341, 1096, 120, 60).xFromCenter()
            false -> Region(342, 1096, 121, 60).xFromCenter()
        }
    }
}