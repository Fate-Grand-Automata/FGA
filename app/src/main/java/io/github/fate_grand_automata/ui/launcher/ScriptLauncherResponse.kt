package io.github.fate_grand_automata.ui.launcher

sealed class ScriptLauncherResponse {
    data object Cancel : ScriptLauncherResponse()
    data class FP(val limit: Int?) : ScriptLauncherResponse()
    data class Lottery(
        val giftBox: GiftBox?
    ) : ScriptLauncherResponse()

    data class GiftBox(
        val maxGoldEmberStackSize: Int,
        val maxGoldEmberTotalCount: Int
    ) : ScriptLauncherResponse()

    data object CEBomb : ScriptLauncherResponse()
    data object SupportImageMaker : ScriptLauncherResponse()
    data object Battle : ScriptLauncherResponse()
}

class ScriptLauncherResponseBuilder(
    val canBuild: () -> Boolean,
    val build: () -> ScriptLauncherResponse
)