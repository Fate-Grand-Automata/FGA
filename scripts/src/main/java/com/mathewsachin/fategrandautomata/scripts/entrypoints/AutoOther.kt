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
        val lotteryCheckRegion = Region(280, 870, 60, 100)

        1.seconds.wait()

        when {
            images.friendSummon in Game.friendPtSummonCheck || images.fpSummonContinue in Game.continueSummonRegion ->
                fp.get().script()
            images.finishedLotteryBox in lotteryCheckRegion || images.finishedLotteryBox in Game.finishedLotteryBoxRegion ->
                lottery.get().script()
            images.goldXP in screenArea || images.silverXP in screenArea ->
                giftBox.get().script()
            images.supportRegionTool in Game.supportRegionToolSearchRegion ->
                supportImageMaker.get().script()
            else -> throw ScriptExitException("Couldn't detect Script type")
        }
    }
}