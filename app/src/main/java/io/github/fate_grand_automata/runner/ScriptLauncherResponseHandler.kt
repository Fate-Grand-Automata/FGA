package io.github.fate_grand_automata.runner

import dagger.hilt.android.scopes.ServiceScoped
import io.github.fate_grand_automata.scripts.enums.ScriptModeEnum
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.launcher.ScriptLauncherResponse
import javax.inject.Inject

@ServiceScoped
class ScriptLauncherResponseHandler @Inject constructor(
    private val prefs: IPreferences
) {
    private fun handleGiftBoxResponse(resp: ScriptLauncherResponse.GiftBox) {
        prefs.maxGoldEmberStackSize = resp.maxGoldEmberStackSize
        prefs.maxGoldEmberTotalCount = resp.maxGoldEmberTotalCount
    }

    fun handle(resp: ScriptLauncherResponse) {
        prefs.scriptMode = when (resp) {
            ScriptLauncherResponse.Cancel -> return

            is ScriptLauncherResponse.FP -> ScriptModeEnum.FP

            is ScriptLauncherResponse.Lottery -> {
                val giftBoxResp = resp.giftBox
                prefs.receiveEmbersWhenGiftBoxFull = giftBoxResp != null

                giftBoxResp?.let { handleGiftBoxResponse(it) }

                ScriptModeEnum.Lottery
            }

            is ScriptLauncherResponse.GiftBox -> {
                handleGiftBoxResponse(resp)

                ScriptModeEnum.PresentBox
            }

            ScriptLauncherResponse.SupportImageMaker -> ScriptModeEnum.SupportImageMaker
            ScriptLauncherResponse.CEBomb -> ScriptModeEnum.CEBomb
            is ScriptLauncherResponse.Battle -> {
                ScriptModeEnum.Battle
            }

            is ScriptLauncherResponse.SkillUpgrade -> {

                prefs.skillUpgrade.shouldUpgradeSkill1 = resp.shouldUpgradeSkill1
                prefs.skillUpgrade.upgradeSkill1 = resp.upgradeSkill1

                prefs.skillUpgrade.shouldUpgradeSkill2 = resp.shouldUpgradeSkill2
                prefs.skillUpgrade.upgradeSkill2 = resp.upgradeSkill2

                prefs.skillUpgrade.shouldUpgradeSkill3 = resp.shouldUpgradeSkill3
                prefs.skillUpgrade.upgradeSkill3 = resp.upgradeSkill3

                ScriptModeEnum.SkillUpgrade
            }

            is ScriptLauncherResponse.ServantEnhancement -> ScriptModeEnum.ServantLevel

            is ScriptLauncherResponse.PlayButtonDetection ->
                ScriptModeEnum.PlayButtonDetection
        }
    }
}