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
            is ScriptLauncherResponse.Append -> {
                // Do not remove this, we need this to only be updated once the user has pressed the "Ok" button
                // otherwise, the config will carry over if the user selected new servant which the config does not
                // match
                prefs.append.shouldUnlockAppendOne = resp.shouldUnlockAppend1
                prefs.append.shouldUnlockAppendTwo = resp.shouldUnlockAppend2
                prefs.append.shouldUnlockAppendThree = resp.shouldUnlockAppend3

                prefs.append.upgradeAppendOne = resp.upgradeAppend1
                prefs.append.upgradeAppendTwo = resp.upgradeAppend2
                prefs.append.upgradeAppendThree = resp.upgradeAppend3

                ScriptModeEnum.Append
            }

            is ScriptLauncherResponse.Battle -> {
                ScriptModeEnum.Battle
            }

            is ScriptLauncherResponse.ServantEnhancement -> {
                ScriptModeEnum.ServantLevel
            }
        }
    }
}