package com.mathewsachin.fategrandautomata.runner

import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.launcher.ScriptLauncherResponse
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class ScriptLauncherResponseHandler @Inject constructor(
    private val prefs: IPreferences
) {
    private fun handleGiftBoxResponse(resp: ScriptLauncherResponse.GiftBox) {
        prefs.maxGoldEmberSetSize = resp.maxGoldEmberStackSize
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

                prefs.refill.updateResources(resp.refillResources)
                prefs.refill.repetitions = resp.refillCount

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