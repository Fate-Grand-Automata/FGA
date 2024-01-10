package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class ServantLevelLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    val emberConfirmationDialogRegion =
        Region(341, 1229, 120, 60).xFromCenter()

    val emberConfirmationDialogLocation = when (isWide) {
        true -> Location(-1096, 1259).xFromRight()
        false -> Location(-1096, 1259).xFromRight()
    }

    val servantAutoSelectRegion = when (isWide) {
        true -> Region(1032, 268, 238, 53).xFromCenter()
        false -> Region(1032, 308, 238, 53).xFromCenter()
    }
    val autoSelectLocation = when (isWide) {
        true -> Location(-347, 294).xFromRight()
        false -> Location(-127, 334).xFromRight()
    }

    fun emptyEmberOrQPDialogRegion(server: GameServer) = when (server) {
        is GameServer.En -> Region(-109, 1088, 218, 72).xFromCenter()
        // JP Option
        else -> Region(-111, 1088, 218, 72).xFromCenter()
    }

    val finalConfirmRegion = Region(341, 1145, 120, 60).xFromCenter()

    val servantMaxLevelRegion = when (isWide) {
        true -> Region(615, 1009, 54, 124).xFromCenter()
        false -> Region(615, 1049, 54, 124).xFromCenter()
    }

    val servantRedirectCheckRegion = when(isWide) {
        true -> Region(689, 1015, 43, 111).xFromCenter()
        false -> Region(689, 1060, 43, 111).xFromCenter()
    }

    val autoSelectMinEmberLowQPRegion = when (isWide) {
        true -> Region(341, 1094, 120, 60).xFromCenter()
        false -> Region(342, 1096, 121, 60).xFromCenter()
    }

    val autoSelectMinEmberLowQPLocation = Location(402, 1126).xFromCenter()

    val returnToServantMenuFromAscensionLocation = Location(816, 572).xFromCenter()

    val ascensionReturnToLevelRegion = when(isWide){
        true -> Region(568, 489, 43, 82).xFromCenter()
        false -> Region(568, 529, 43, 82).xFromCenter()
    }
}