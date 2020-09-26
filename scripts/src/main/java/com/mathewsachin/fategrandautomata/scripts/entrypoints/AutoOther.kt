package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.StorageDirs
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.seconds

class AutoOther @Inject constructor(
    val storageDirs: StorageDirs,
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
                AutoFriendGacha(exitManager, platformImpl, this).script()
            images.finishedLotteryBox in lotteryCheckRegion || images.finishedLotteryBox in Game.finishedLotteryBoxRegion ->
                AutoLottery(exitManager, platformImpl, this).script()
            images.goldXP in screenArea || images.silverXP in screenArea ->
                AutoGiftBox(exitManager, platformImpl, this).script()
            images.supportRegionTool in Game.supportRegionToolSearchRegion ->
                SupportImageMaker(storageDirs, exitManager, platformImpl, this).script()
            else -> throw ScriptExitException("Couldn't detect Script type")
        }
    }
}