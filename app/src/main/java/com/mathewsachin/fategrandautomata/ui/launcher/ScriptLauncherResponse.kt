package com.mathewsachin.fategrandautomata.ui.launcher

import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig

sealed class ScriptLauncherResponse {
    object Cancel : ScriptLauncherResponse()
    data class FP(val limit: Int?) : ScriptLauncherResponse()
    data class Lottery(
        val giftBox: GiftBox?
    ) : ScriptLauncherResponse()

    data class GiftBox(val maxGoldEmberStackSize: Int) : ScriptLauncherResponse()
    data class CEBomb(val targetRarity: Int) : ScriptLauncherResponse()
    object SupportImageMaker : ScriptLauncherResponse()
    data class Battle(
        val config: IBattleConfig,
        val refillResources: Set<RefillResourceEnum>,
        val refillCount: Int,
        val limitRuns: Int?,
        val limitMats: Int?,
        val limitCEs: Int?,
        val waitApRegen: Boolean
    ) : ScriptLauncherResponse()
}

class ScriptLauncherResponseBuilder(
    val canBuild: () -> Boolean,
    val build: () -> ScriptLauncherResponse
)