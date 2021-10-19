package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoDetect @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
) : IFgoAutomataApi by fgAutomataApi {
    fun get() = useSameSnapIn {
        val emberSearchRegion = game.scriptArea.let {
            it.copy(width = it.width / 3)
        }

        when {
            images[Images.FriendSummon] in game.fp.summonCheck || images[Images.FPSummonContinue] in game.fp.continueSummonRegion ->
                ScriptModeEnum.FP
            images[Images.LotteryBoxFinished] in game.lottery.checkRegion || images[Images.LotteryBoxFinished] in game.lottery.finishedRegion ->
                ScriptModeEnum.Lottery
            images[Images.GoldXP] in emberSearchRegion || images[Images.SilverXP] in emberSearchRegion ->
                ScriptModeEnum.PresentBox
            images[Images.SupportConfirmSetupButton] in game.support.confirmSetupButtonRegion ->
                ScriptModeEnum.SupportImageMaker
            images[Images.CEEnhance] in game.ceEnhanceRegion ->
                ScriptModeEnum.CEBomb
            else -> ScriptModeEnum.Battle
        }
    }
}