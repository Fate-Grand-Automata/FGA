package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class ServantEnhancementLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    val getServantEnhancementRegion = when (isWide) {
        true -> Region(-551, 38, 357, 75).xFromRight()
        false -> Region(-381, 38, 357, 75).xFromRight()
    }

    val getEmberConfirmationDialogRegion = Region(341, 1229, 120, 60).xFromCenter()

    val emberConfirmationDialogLocation = when (isWide) {
        true -> Location(-1096, 1259).xFromRight()
        false -> Location(-1096, 1259).xFromRight()
    }

    val getAutoSelectRegion = when (isWide) {
        true -> Region(-470, 262, 242, 61).xFromRight()
        false -> Region(-250, 304, 242, 61).xFromRight()
    }
    val autoSelectLocation = when (isWide) {
        true -> Location(-347, 294).xFromRight()
        false -> Location(-127, 334).xFromRight()
    }

    fun getNoEmberOrQPDialogRegion(server: GameServer) = when (server) {
        is GameServer.En -> Region(-109, 1088, 218, 72).xFromCenter()
        is GameServer.Jp -> Region(-111, 1088, 218, 72).xFromCenter()
        // Other servers are not supported
        else -> Region(-109, 1088, 218, 72).xFromCenter()
    }

    val getFinalConfirmRegion = Region(341, 1145, 120, 60).xFromCenter()

    val getFinalConfirmLocation = Location(401, 1175).xFromCenter()

    val getServantMaxLevelRegion = when (isWide) {
        true -> Region(615, 1009, 54, 124).xFromCenter()
        false -> Region(615, 1049, 54, 124).xFromCenter()
    }

    val getAutoSelectNoQPRegion = Region(342, 1096, 121, 60).xFromCenter()
}