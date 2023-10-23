package io.github.fate_grand_automata.ui.launcher

sealed class ScriptLauncherResponse {
    object Cancel : ScriptLauncherResponse()
    data class FP(val limit: Int?) : ScriptLauncherResponse()
    data class Lottery(
        val giftBox: GiftBox?
    ) : ScriptLauncherResponse()

    data class GiftBox(
        val maxGoldEmberStackSize: Int,
        val maxGoldEmberTotalCount: Int
    ) : ScriptLauncherResponse()

    data class CEBomb(val targetRarity: Int) : ScriptLauncherResponse()
    object SupportImageMaker : ScriptLauncherResponse()
    object Battle : ScriptLauncherResponse()
}

class ScriptLauncherResponseBuilder(
    val canBuild: () -> Boolean,
    val build: () -> ScriptLauncherResponse
)