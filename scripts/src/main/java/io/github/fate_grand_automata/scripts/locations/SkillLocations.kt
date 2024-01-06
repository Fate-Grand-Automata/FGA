package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

class SkillLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {


    fun skillLocation(skill: Int) = Location(-339, 519).xFromCenter() +
            Location(576 * (skill - 1), 0)

    val getInsufficientMatsRegion = when (gameServer) {
        is GameServer.En -> when (isWide) {
            true -> Region(-498, 197, 446, 43).xFromCenter()
            false -> Region(-498, 225, 446, 43).xFromCenter()
        }
        // JP option
        else -> when (isWide) {
            true -> Region(-499, 200, 597, 40).xFromCenter()
            false -> Region(-501, 228, 597, 43).xFromCenter()
        }
    }


    val confirmationDialogRegion = Region(341, 1146, 121, 60).xFromCenter()


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