package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.seconds

class AutoOther @Inject constructor(
    val fp: Provider<AutoFriendGacha>,
    val lottery: Provider<AutoLottery>,
    val giftBox: Provider<AutoGiftBox>,
    val supportImageMaker: Provider<SupportImageMaker>,
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFgoAutomataApi by fgAutomataApi {
    override fun script(): Nothing {
        val screenArea = Region(Location(), Game.scriptSize)
        val lotteryCheckRegion = Region(150, 800, 340, 230)

        1.seconds.wait()

        val entryPoint = when {
            images.friendSummon in Game.friendPtSummonCheck || images.fpSummonContinue in Game.continueSummonRegion ->
                fp.get()
            images.finishedLotteryBox in lotteryCheckRegion || images.finishedLotteryBox in Game.finishedLotteryBoxRegion ->
                lottery.get()
            images.goldXP in screenArea || images.silverXP in screenArea ->
                giftBox.get()
            images.supportRegionTool in Game.supportRegionToolSearchRegion ->
                supportImageMaker.get()
            else -> throw ScriptExitException(messages.cannotDetectScriptType)
        }

        entryPoint.script()
    }
}