package io.github.fate_grand_automata.ui.launcher

sealed class ScriptLauncherResponse {

    object Cancel : ScriptLauncherResponse()
    data object FP : ScriptLauncherResponse()

    data class Lottery(
        val giftBox: GiftBox?
    ) : ScriptLauncherResponse()

    data class GiftBox(
        val maxGoldEmberStackSize: Int,
        val maxGoldEmberTotalCount: Int
    ) : ScriptLauncherResponse()

    data class CEBomb(val targetRarity: Int) : ScriptLauncherResponse()
    data object SupportImageMaker : ScriptLauncherResponse()
    data object Battle : ScriptLauncherResponse()

    data object ServantEnhancement : ScriptLauncherResponse()
}

class ScriptLauncherResponseBuilder(
    val canBuild: () -> Boolean,
    val build: () -> ScriptLauncherResponse
)