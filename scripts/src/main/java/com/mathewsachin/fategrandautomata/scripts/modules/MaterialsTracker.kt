package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class MaterialsTracker @Inject constructor(
    api: IFgoAutomataApi,
    private val battleConfig: IBattleConfig,
    private val state: BattleState
) : IFgoAutomataApi by api {
    // Set all Materials to 0
    private var matsGot =
        battleConfig
            .materials
            .associateWith { 0 }
            .toMutableMap()

    val farmed: Map<MaterialEnum, Int> get() = matsGot

    fun autoDecrement() {
        val refill = prefs.refill

        // Auto-decrement materials
        if (refill.shouldLimitMats) {
            refill.limitMats -= matsGot.values.sum()

            // Turn off limit by materials when done
            if (refill.limitMats <= 0) {
                refill.limitMats = 1
                refill.shouldLimitMats = false
            }
        }
    }

    fun parseMaterials() {
        for (material in battleConfig.materials) {
            val pattern = images.loadMaterial(material)

            // TODO: Make the search region smaller
            val count = locations.scriptArea
                .findAll(pattern)
                .count()

            // Increment material count
            matsGot.merge(material, count, Int::plus)
        }

        if (prefs.refill.shouldLimitMats) {
            val totalMats = matsGot
                .values
                .sum()

            if (totalMats >= prefs.refill.limitMats) {
                // Count the current run
                state.nextRun()

                throw AutoBattle.BattleExitException(AutoBattle.ExitReason.LimitMaterials(totalMats))
            }
        }
    }
}