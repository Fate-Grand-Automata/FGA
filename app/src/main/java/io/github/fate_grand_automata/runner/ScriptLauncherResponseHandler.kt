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

                prefs.loopIntoLotteryAfterPresentBox = resp.returnToLottery

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
                // Do not remove this, we need this to only be updated once the user has pressed the "Ok" button
                // otherwise, the config will carry over if the user selected new servant which the config does not
                // match (e.g. skill 3 can be upgraded to servant A but not to servant B)
                prefs.skillUpgrade.shouldUpgradeSkillOne = resp.shouldUpgradeSkill1
                prefs.skillUpgrade.skillOneUpgradeValue = resp.upgradeSkill1

                prefs.skillUpgrade.shouldUpgradeSkillTwo = resp.shouldUpgradeSkill2
                prefs.skillUpgrade.skillTwoUpgradeValue = resp.upgradeSkill2

                prefs.skillUpgrade.shouldUpgradeSkillThree = resp.shouldUpgradeSkill3
                prefs.skillUpgrade.skillThreeUpgradeValue = resp.upgradeSkill3

                ScriptModeEnum.Skill
            }

            is ScriptLauncherResponse.ServantEnhancement -> ScriptModeEnum.ServantLevel

            is ScriptLauncherResponse.PlayButtonDetection ->
                ScriptModeEnum.PlayButtonDetection

            is ScriptLauncherResponse.Append -> {
                // Do not remove this, we need this to only be updated once the user has pressed the "Ok" button
                // otherwise, the config will carry over if the user selected new servant which the config does not
                // match
                prefs.append.shouldUnlockAppend1 = resp.shouldUnlockAppend1
                prefs.append.shouldUnlockAppend2 = resp.shouldUnlockAppend2
                prefs.append.shouldUnlockAppend3 = resp.shouldUnlockAppend3

                prefs.append.upgradeAppend1 = resp.upgradeAppend1
                prefs.append.upgradeAppend2 = resp.upgradeAppend2
                prefs.append.upgradeAppend3 = resp.upgradeAppend3

                ScriptModeEnum.Append
            }

            is ScriptLauncherResponse.NotifyError -> ScriptModeEnum.NotifyError
        }
    }
}