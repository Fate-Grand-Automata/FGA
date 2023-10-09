package io.github.fate_grand_automata.ui.launcher

import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.fate_grand_automata.scripts.prefs.IPerServerConfigPrefs

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

    object SupportImageMaker : ScriptLauncherResponse()
    data class Battle(
        val config: IBattleConfig,
        val perServerConfigPref: IPerServerConfigPrefs,
        val refillResources: Set<RefillResourceEnum>,
        val rainbowRefillCount: Int,
        val goldRefillCount: Int,
        val silverRefillCount: Int,
        val blueRefillCount: Int,
        val copperRefillCount: Int,
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