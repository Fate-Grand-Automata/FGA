package io.github.fate_grand_automata.ui.launcher

sealed class ScriptLauncherResponse {
    data object Cancel : ScriptLauncherResponse()
    data object FP : ScriptLauncherResponse()
    data class Lottery(
        val giftBox: GiftBox?
    ) : ScriptLauncherResponse()

    data class GiftBox(
        val maxGoldEmberStackSize: Int,
        val maxGoldEmberTotalCount: Int
    ) : ScriptLauncherResponse()

    data object CEBomb : ScriptLauncherResponse()

    data class SkillUpgrade(
        val shouldUpgradeSkill1: Boolean,
        val upgradeSkill1: Int,
        val shouldUpgradeSkill2: Boolean,
        val upgradeSkill2: Int,
        val shouldUpgradeSkill3: Boolean,
        val upgradeSkill3: Int,
    ) : ScriptLauncherResponse()

    data class ServantEnhancement(
        val shouldLimit: Boolean,
        val limitCount: Int
    ): ScriptLauncherResponse()

    data object PlayButtonDetection : ScriptLauncherResponse()

    data object SupportImageMaker : ScriptLauncherResponse()
    data object Battle : ScriptLauncherResponse()
}

class ScriptLauncherResponseBuilder(
    val canBuild: () -> Boolean,
    val build: () -> ScriptLauncherResponse
)