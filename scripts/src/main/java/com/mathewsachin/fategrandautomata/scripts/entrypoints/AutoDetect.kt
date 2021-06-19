package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.ScriptModeEnum
import javax.inject.Inject

class AutoDetect @Inject constructor(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi {
    fun get() = useSameSnapIn {
        val emberSearchRegion = game.scriptArea.let {
            it.copy(Width = it.Width / 3)
        }

        when {
            images[Images.FriendSummon] in game.fpSummonCheck || images[Images.FPSummonContinue] in game.fpContinueSummonRegion ->
                ScriptModeEnum.FP
            images[Images.LotteryBoxFinished] in game.lotteryCheckRegion || images[Images.LotteryBoxFinished] in game.lotteryFinishedRegion ->
                ScriptModeEnum.Lottery
            images[Images.GoldXP] in emberSearchRegion || images[Images.SilverXP] in emberSearchRegion ->
                ScriptModeEnum.PresentBox
            images[Images.SupportRegionTool] in game.supportRegionToolSearchRegion ->
                ScriptModeEnum.SupportImageMaker
            images[Images.CEEnhance] in game.ceEnhanceRegion ->
                ScriptModeEnum.CEBomb
            else -> ScriptModeEnum.Battle
        }
    }
}