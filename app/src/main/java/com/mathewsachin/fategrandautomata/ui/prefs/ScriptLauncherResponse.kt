package com.mathewsachin.fategrandautomata.ui.prefs

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig

sealed class ScriptLauncherResponse {
    object Cancel: ScriptLauncherResponse()
    data class FP(val limit: Int?): ScriptLauncherResponse()
    object Lottery: ScriptLauncherResponse()
    data class GiftBox(val maxGoldEmberStackSize: Int): ScriptLauncherResponse()
    object SupportImageMaker: ScriptLauncherResponse()
    data class Battle(
        val config: IBattleConfig,
        val refillResources: Set<RefillResourceEnum>,
        val refillCount: Int,
        val limitRuns: Int?,
        val limitMats: Int?
    ): ScriptLauncherResponse()
}

class ScriptLauncherResponseBuilder(
    val canBuild: () -> Boolean,
    val build: () -> ScriptLauncherResponse
)