package io.github.fate_grand_automata.ui.launcher

import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig

sealed class ScriptLauncherResponse {
    object Cancel : ScriptLauncherResponse()
    data class FP(val limit: Int?) : ScriptLauncherResponse()
    data class Lottery(
        val giftBox: GiftBox?
    ) : ScriptLauncherResponse()

    data class GiftBox(val maxGoldEmberStackSize: Int) : ScriptLauncherResponse()
    object CEBomb : ScriptLauncherResponse()
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