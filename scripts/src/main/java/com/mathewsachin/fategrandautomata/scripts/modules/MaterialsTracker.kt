package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.libautomata.dagger.ScriptScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ScriptScope
class MaterialsTracker @Inject constructor(
    api: IFgoAutomataApi,
    private val battleConfig: IBattleConfig,
    private val state: BattleState
) : IFgoAutomataApi by api {
    // Set all wanted Materials to 0, additional drops are added on demand
    private var matsGot = battleConfig
        .materials
        .associateWith { 0 }
        .toMutableMap()
    val wantedMats get() = matsGot.filterKeys(battleConfig.materials::contains)
    val farmed: Map<MaterialEnum, Int> get() = matsGot

    fun autoDecrement() {
        val refill = prefs.refill

        // Auto-decrement materials
        if (refill.shouldLimitMats) {
            refill.limitMats -= wantedMats.values.sum()

            // Turn off limit by materials when done
            if (refill.limitMats <= 0) {
                refill.limitMats = 1
                refill.shouldLimitMats = false
            }
        }
    }

    fun parseMaterials() = runBlocking {
        MaterialEnum.values().map { material ->
            async {
                val pattern = images.loadMaterial(material)

                // TODO: Make the search region smaller
                val count = locations.scriptArea
                    .findAll(pattern)
                    .count()

                // Increment material count
                if (count > 0) {
                    matsGot.merge(material, count, Int::plus)
                }
            }
        }.awaitAll();

        if (prefs.refill.shouldLimitMats) {
            val totalMats = wantedMats.values.sum()

            if (totalMats >= prefs.refill.limitMats) {
                // Count the current run
                state.nextRun()

                throw AutoBattle.BattleExitException(AutoBattle.ExitReason.LimitMaterials(totalMats))
            }
        }
    }
}