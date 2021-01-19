package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
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
            images.friendSummon in game.fpSummonCheck || images.fpSummonContinue in game.fpContinueSummonRegion ->
                ScriptModeEnum.FP
            images.finishedLotteryBox in game.lotteryCheckRegion || images.finishedLotteryBox in game.lotteryFinishedRegion ->
                ScriptModeEnum.Lottery
            images.goldXP in emberSearchRegion || images.silverXP in emberSearchRegion ->
                ScriptModeEnum.PresentBox
            images.supportRegionTool in game.supportRegionToolSearchRegion ->
                ScriptModeEnum.SupportImageMaker
            else -> ScriptModeEnum.Battle
        }
    }
}