package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.ScriptModeEnum
import io.github.fate_grand_automata.scripts.modules.AutoSetup
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoDetect @Inject constructor(
    api: IFgoAutomataApi,
    private val autoSetup: AutoSetup
) : IFgoAutomataApi by api {
    fun get() = useSameSnapIn {
        val emberSearchRegion = locations.scriptArea.let {
            it.copy(width = it.width / 3)
        }
        val isPlayButtonInGoodXLocation = autoSetup.isPlayButtonInGoodXLocation
        val isPlayButtonInGoodYLocation = autoSetup.isPlayButtonInGoodYLocation

        when {
            (!isPlayButtonInGoodXLocation || !isPlayButtonInGoodYLocation) &&
                    !prefs.ignorePlayButtonDetectionWarning ->
                ScriptModeEnum.PlayButtonDetection

            images[Images.ServantAutoSelectOff] in locations.servant.servantAutoSelectRegion -> {
                autoSetup.noServantFoundWarning(ScriptModeEnum.ServantLevel)
                ScriptModeEnum.NotifyError
            }

            images[Images.FriendSummon] in locations.fp.summonCheck ||
                    findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue) ||
                    images[Images.FriendSummon] in locations.fp.initialSummonCheck ->
                ScriptModeEnum.FP

            images[Images.LotteryBoxFinished] in locations.lottery.checkRegion ||
                    images[Images.LotteryBoxFinished] in locations.lottery.finishedRegion ->
                ScriptModeEnum.Lottery

            listOf(images[Images.GoldXP], images[Images.SilverXP]) in emberSearchRegion ->
                ScriptModeEnum.PresentBox

            locations.support.confirmSetupButtonRegion.exists(
                images[Images.SupportConfirmSetupButton], similarity = 0.75
            ) ->
                ScriptModeEnum.SupportImageMaker

            images[Images.CraftEssenceEnhancement] in locations.ceBomb.getCeEnhanceRegion -> {
                autoSetup.checkIfEmptyEnhance()
                autoSetup.checkIfCanAutomaticDisplayChangeInCE()
                ScriptModeEnum.CEBomb
            }

            images[Images.SkillEnhancement] in locations.skillUpgrade.getSkillEnhanceRegion -> {
                autoSetup.getMinimumSkillLevel()
                ScriptModeEnum.SkillUpgrade
            }

            images[Images.ServantAutoSelect] in locations.servant.servantAutoSelectRegion ->
                ScriptModeEnum.ServantLevel

            images[Images.AppendSkill] in locations.append.getAppendBannerRegion -> {
                autoSetup.checkAppendLocks()
                ScriptModeEnum.Append
            }

            else -> ScriptModeEnum.Battle
        }
    }
}