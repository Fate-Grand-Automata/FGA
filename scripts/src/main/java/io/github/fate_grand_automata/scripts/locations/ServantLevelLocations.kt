package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class ServantLevelLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    val emberConfirmationDialogRegion = when (gameServer) {
        is GameServer.Tw -> Region(338, 1229, 130, 70).xFromCenter()
        else -> Region(250, 1204, 300, 116).xFromCenter()
    }

    val servantAutoSelectRegion = when (gameServer) {
        // TW doesn't have the automatic autofill yet
        // Also, its templates fill the whole button, so the region needs margin around it to still match when the button is shifted slightly
        is GameServer.Tw -> Region(1022, 0, 258, 73).xFromCenter().copy(y = if (isWide) 258 else 298)
        else -> Region(742, 0, 304, 72).xFromCenter().copy(y = if (isWide) 268 else 308)
    }

    val emptyEmberOrQPDialogRegion =
        Region(-113, 1086, 224, 76).xFromCenter()

    /**
     * The OK button of the final confirmation dialog, with margin around the label because the OK
     * templates are cropped at different offsets from their text, e.g. KR's "확인" and "결정".
     */
    val finalConfirmRegion = Region(321, 1125, 170, 100).xFromCenter()

    val servantMaxLevelRegion = when (isWide) {
        true -> Region(613, 1007, 58, 128).xFromCenter()
        false -> Region(613, 1047, 58, 128).xFromCenter()
    }

    fun servantRedirectCheckRegion(server: GameServer) = when(server){
        is GameServer.En, is GameServer.Jp -> when(isWide) {
            true -> Region(687, 1013, 47, 115).xFromCenter()
            false -> Region(687, 1058, 47, 115).xFromCenter()
        }
        // CN, TW, KR
        else -> when(isWide){
            true -> Region(774, 1013, 50, 114).xFromCenter()
            false -> Region(774, 1058, 50, 114).xFromCenter()
        }
    }

    /**
     * The OK button of the dialog offering to auto-select fewer embers when QP is low, with the
     * same margin as [finalConfirmRegion].
     */
    val autoSelectMinEmberLowQPRegion = Region(321, 1074, 170, 100).xFromCenter()

    val autoSelectMinEmberLowQPLocation = Location(402, 1126).xFromCenter()

    val returnToServantMenuFromAscensionLocation = Location(816, 572).xFromCenter()

    val ascensionReturnToLevelRegion = when(isWide){
        true -> Region(566, 487, 47, 86).xFromCenter()
        false -> Region(566, 527, 47, 86).xFromCenter()
    }

    val autoFillStateRegion = Region(265, 836, 100, 80).xFromCenter()
}