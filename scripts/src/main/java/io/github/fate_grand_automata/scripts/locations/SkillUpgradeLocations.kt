package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class SkillUpgradeLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {


    fun getSkillEnhanceRegion(server: GameServer) = when (server) {
        is GameServer.En -> when (isWide) {
            false -> Region(-240, 19, 213, 86).xFromRight()
            true -> Region(-408, 20, 213, 86).xFromRight()
        }

        is GameServer.Jp -> when (isWide) {
            false -> Region(-1154, 20, 897, 100).xFromRight()
            true -> Region(-1324, 20, 897, 100).xFromRight()
        }

        else -> Region(-408, 20, 213, 86).xFromRight()
    }


    val skill1Region = when (isWide) {
        true -> Region(-437, 402, 195, 194).xFromCenter()
        false -> Region(-437, 422, 195, 194).xFromCenter()
    }
    val skill1Click = Location(-339, 519).xFromCenter()

    val skill2Region = when (isWide) {
        true -> Region(139, 402, 195, 194).xFromCenter()
        false -> Region(139, 422, 195, 194).xFromCenter()
    }
    val skill2Click = Location(236, 519).xFromCenter()

    val skill3Region = when (isWide) {
        true -> Region(715, 402, 195, 194).xFromCenter()
        false -> Region(715, 422, 195, 194).xFromCenter()
    }
    val skill3Click = Location(812, 519).xFromCenter()

    fun getInsufficientMatsRegion(server: GameServer) = when (server) {
        is GameServer.En -> when (isWide) {
            true -> Region(-498, 197, 446, 43).xFromCenter()
            false -> Region(-498, 225, 446, 43).xFromCenter()
        }

        is GameServer.Jp -> when (isWide) {
            true -> Region(-499, 200, 597, 40).xFromCenter()
            false -> Region(-501, 228, 597, 43).xFromCenter()
        }

        else -> Region(-498, 225, 286, 43).xFromCenter()
    }

    val enhancementClick = when (isWide) {
        false -> Location(-281, 1343).xFromRight()
        true -> Location(-396, 1284).xFromRight()
    }


    fun getConfirmationDialog(server: GameServer) = when (server) {
        is GameServer.En -> Region(341, 1146, 121, 60).xFromCenter()
        is GameServer.Jp -> Region(341, 1146, 121, 60).xFromCenter()
        else -> Region(341, 1146, 121, 60).xFromCenter()
    }

    val confirmationDialogClick = Location(401, 1176).xFromCenter()

    val skipRapidClick = Location(0, 1400).xFromCenter()

    val skill1TextRegion = when (isWide) {
        true -> Region(-100, 545, 64, 56).xFromCenter()
        false -> Region(-100, 585, 64, 56).xFromCenter()
    }


    val skill2TextRegion = when (isWide) {
        true -> Region(474, 545, 64, 56).xFromCenter()
        false -> Region(474, 585, 64, 56).xFromCenter()
    }

    val skill3TextRegion = when (isWide) {
        true -> Region(1049, 545, 68, 56).xFromCenter()
        false -> Region(1049, 585, 68, 56).xFromCenter()
    }
}