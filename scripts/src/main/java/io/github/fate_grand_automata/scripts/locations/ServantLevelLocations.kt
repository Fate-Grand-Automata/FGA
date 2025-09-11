package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class ServantLevelLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms,
) : IScriptAreaTransforms by scriptAreaTransforms {

    val emberConfirmationDialogRegion = when (gameServer) {
        is GameServer.Jp -> Region(321, 1209, 160, 100).xFromCenter()
        else -> Region(341, 1229, 120, 60).xFromCenter()
    }

    val emberConfirmationDialogLocation = when (isWide) {
        true -> Location(-1096, 1259).xFromRight()
        false -> Location(-1096, 1259).xFromRight()
    }

    val servantAutoSelectRegion = when (gameServer) {
        is GameServer.Jp -> Region(764, 0, 260, 60).xFromCenter()
        else -> Region(1032, 0, 238, 53).xFromCenter()
    }.copy(y = if (isWide) 268 else 308)

    val autoSelectLocation = when (isWide) {
        true -> Location(-347, 294).xFromRight()
        false -> Location(-127, 334).xFromRight()
    }

    val emptyEmberOrQPDialogRegion =
        Region(-113, 1086, 224, 76).xFromCenter()

    val finalConfirmRegion = Region(339, 1143, 124, 64).xFromCenter()

    val servantMaxLevelRegion = when (isWide) {
        true -> Region(613, 1007, 58, 128).xFromCenter()
        false -> Region(613, 1047, 58, 128).xFromCenter()
    }

    fun servantRedirectCheckRegion(server: GameServer) = when (server) {
        is GameServer.En, is GameServer.Jp -> when (isWide) {
            true -> Region(687, 1013, 47, 115).xFromCenter()
            false -> Region(687, 1058, 47, 115).xFromCenter()
        }
        // CN, TW, KR
        else -> when (isWide) {
            true -> Region(774, 1013, 50, 114).xFromCenter()
            false -> Region(774, 1058, 50, 114).xFromCenter()
        }
    }

    val autoSelectMinEmberLowQPRegion = Region(339, 1092, 128, 66).xFromCenter()

    val autoSelectMinEmberLowQPLocation = Location(402, 1126).xFromCenter()

    val returnToServantMenuFromAscensionLocation = Location(816, 572).xFromCenter()

    val ascensionReturnToLevelRegion = when (isWide) {
        true -> Region(566, 487, 47, 86).xFromCenter()
        false -> Region(566, 527, 47, 86).xFromCenter()
    }

    val autoFillStateRegion = Region(265, 836, 100, 80).xFromCenter()
}
