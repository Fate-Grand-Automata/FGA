package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.lib_automata.Location
import io.github.lib_automata.Region
import javax.inject.Inject

/**
 * Locations on the Skill Enhancement screen, where the three normal skills sit side by side.
 */
class SkillLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
) : IScriptAreaTransforms by scriptAreaTransforms {

    /**
     * The skill icon to select a skill for enhancement.
     * @param skillNumber 1, 2 or 3
     */
    fun skillLocation(skillNumber: Int) = Location(-339, 519).xFromCenter() +
            Location(576 * (skillNumber - 1), 0)

    /**
     * The "Lv. N/10" text next to a skill icon, wide enough for "Lv. 10/10". It is empty when the
     * skill is locked.
     * @param skillNumber 1, 2 or 3
     */
    fun skillLevelRegion(skillNumber: Int) =
        Region(-200, if (isWide) 545 else 585, 276, 56).xFromCenter() +
                Location(575 * (skillNumber - 1), 0)

    val insufficientMaterialsRegion = when (gameServer) {
        is GameServer.En -> when (isWide) {
            true -> Region(-498, 197, 446, 43).xFromCenter()
            false -> Region(-498, 225, 446, 43).xFromCenter()
        }
        else -> when (isWide) {
            true -> Region(-499, 200, 597, 40).xFromCenter()
            false -> Region(-501, 228, 597, 43).xFromCenter()
        }
    }

    val insufficientQPRegion = when (gameServer) {
        is GameServer.En -> when (isWide) {
            true -> Region(-500, 195, 405, 44).xFromCenter()
            false -> Region(-499, 222, 405, 44).xFromCenter()
        }
        else -> when (isWide) {
            true -> Region(-502, 195, 397, 47).xFromCenter()
            false -> Region(-500, 222, 397, 47).xFromCenter()
        }
    }

    val confirmationDialogRegion = Region(341, 1146, 121, 60).xFromCenter()
}
