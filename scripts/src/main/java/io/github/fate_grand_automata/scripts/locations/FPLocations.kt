package io.github.fate_grand_automata.scripts.locations

import io.github.lib_automata.Location
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FPLocations @Inject constructor(
    scriptAreaTransforms: IScriptAreaTransforms
): IScriptAreaTransforms by scriptAreaTransforms {
    val summonCheck = Region(100, 1152, 75, 143).xFromCenter()
    val initialSummonCheck = Region(-265, 1131, 75, 143).xFromCenter()
    val initialSummonClick = Location(2, 1052).xFromCenter()
    val initialSummonContinueClick = Region(341, 1145, 120, 60).xFromCenter()


    val continueSummonRegion = Region(-36, 1264, 580, 170).xFromCenter()
    val first10SummonClick = Location(120, 1062).xFromCenter()
    val okClick = Location(320, 1120).xFromCenter()
    val continueSummonClick = Location(320, 1325).xFromCenter()
    val skipRapidClick = Location(1240, 1400).xFromCenter()

    val fpSellRegion = when(isWide){
        true -> Region(651, 1262, 318, 109).xFromCenter()
        false -> Region(651, 1291, 318, 109).xFromCenter()
    }

    val sellBannerRegion = when(isWide){
        true -> Region(194, 829, 135, 115)
        false -> Region(6, 847, 135, 115)
    }

    val inventoryFullSellRegion = Region(-647, 923, 114, 54).xFromCenter()
}