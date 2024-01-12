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
                ScriptModeEnum.CEBomb
            }

            is ScriptLauncherResponse.Battle -> {
                ScriptModeEnum.Battle
            }
        }
    }
}