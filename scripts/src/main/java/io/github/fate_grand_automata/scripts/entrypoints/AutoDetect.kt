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
        val isPlayButtonInGoodXLocation = prefs.playButtonLocation.x in
                0..locations.scriptAreaRaw.width / 4
        val isPlayButtonInGoodYLocation = prefs.playButtonLocation.y in
                locations.scriptAreaRaw.height * 5 / 8..locations.scriptAreaRaw.height

        when {
            (!isPlayButtonInGoodXLocation || !isPlayButtonInGoodYLocation) &&
                    !prefs.ignorePlayButtonDetectionWarning ->
                ScriptModeEnum.PlayButtonDetection

            images[Images.FriendSummon] in locations.fp.summonCheck ||
                    findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue) ->
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

            images[Images.CraftEssenceEnhancement] in locations.getCeEnhanceRegion(prefs.gameServer) ->
                ScriptModeEnum.CEBomb

            images[Images.SkillEnhancement] in locations.skillUpgrade.getSkillEnhanceRegion(prefs.gameServer) -> {
                autoSetup.getMinimumSkillLevel()
                ScriptModeEnum.SkillUpgrade
            }

            images[Images.ServantEnhancement] in locations.servant.getServantEnhancementRegion ->
                ScriptModeEnum.ServantLevel

            else -> ScriptModeEnum.Battle
        }
    }
}