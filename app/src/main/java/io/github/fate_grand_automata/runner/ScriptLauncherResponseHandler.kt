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
            is ScriptLauncherResponse.FP -> {
                prefs.shouldLimitFP = resp.limit != null
                resp.limit?.let { prefs.limitFP = it }

                ScriptModeEnum.FP
            }

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
            is ScriptLauncherResponse.CEBomb -> {
                prefs.ceBombTargetRarity = resp.targetRarity

                ScriptModeEnum.CEBomb
            }

            is ScriptLauncherResponse.Battle -> {
                prefs.selectedBattleConfig = resp.config

                prefs.selectedServerConfigPref = resp.perServerConfigPref

                if(resp.refillResources.isNotEmpty()){
                    prefs.selectedServerConfigPref.selectedApple = resp.refillResources.first()
                }

                prefs.selectedServerConfigPref.updateResources(resp.refillResources)

                prefs.selectedServerConfigPref.blueApple = resp.blueRefillCount
                prefs.selectedServerConfigPref.goldApple = resp.goldRefillCount
                prefs.selectedServerConfigPref.silverApple = resp.silverRefillCount
                prefs.selectedServerConfigPref.copperApple = resp.copperRefillCount
                prefs.selectedServerConfigPref.rainbowApple = resp.rainbowRefillCount


                prefs.refill.shouldLimitRuns = resp.limitRuns != null
                resp.limitRuns?.let { prefs.refill.limitRuns = it }

                prefs.refill.shouldLimitMats = resp.limitMats != null
                resp.limitMats?.let { prefs.refill.limitMats = it }

                prefs.refill.shouldLimitCEs = resp.limitCEs != null
                resp.limitCEs?.let { prefs.refill.limitCEs = it }

                prefs.waitAPRegen = resp.waitApRegen

                ScriptModeEnum.Battle
            }
        }
    }
}