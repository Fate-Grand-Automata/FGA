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

        when {
            images[Images.FriendSummon] in locations.fp.summonCheck ||
                    findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue) ||
                    images[Images.FriendSummon] in locations.fp.initialSummonCheck ->
                ScriptModeEnum.FP

            images[Images.LotteryBoxFinished] in locations.lottery.checkRegion || images[Images.LotteryBoxFinished] in locations.lottery.finishedRegion ->
                ScriptModeEnum.Lottery

            listOf(images[Images.GoldXP], images[Images.SilverXP]) in emberSearchRegion ->
                ScriptModeEnum.PresentBox

            locations.support.confirmSetupButtonRegion.exists(images[Images.SupportConfirmSetupButton], similarity = 0.75) ->
                ScriptModeEnum.SupportImageMaker

            mapOf(
                images[Images.ServantAutoSelect] to locations.servant.servantAutoSelectRegion,
                images[Images.ServantAutoSelectOff] to locations.servant.servantAutoSelectRegion,
                images[Images.ServantAscensionBanner] to locations.enhancementBannerRegion
            ).exists()->
                ScriptModeEnum.ServantLevel


            images[Images.AppendBanner] in locations.enhancementBannerRegion -> {
                autoSetup.checkIfEmptyEnhance()
                autoSetup.checkAppendLocks()
                ScriptModeEnum.Append
            }

            images[Images.EmptyEnhance] in locations.emptyEnhanceRegion ->
                ScriptModeEnum.CEBomb

            else -> ScriptModeEnum.Battle
        }
    }
}