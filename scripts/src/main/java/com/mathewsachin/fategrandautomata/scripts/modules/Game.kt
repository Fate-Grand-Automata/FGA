package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.enums.RefillResourceEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.ITransformationExtensions
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.seconds

fun IFgoAutomataApi.needsToRetry() = images.retry in game.retryRegion

fun IFgoAutomataApi.retry() {
    game.retryRegion.click()

    2.seconds.wait()
}

@ScriptScope
class Game @Inject constructor(
    val prefs: IPreferences,
    transformationExtensions: ITransformationExtensions,
    val gameAreaManager: GameAreaManager
) {
    companion object {
        val menuScreenRegion = Region(2100, 1200, 1000, 1000)
        val menuStorySkipRegion = Region(2240, 20, 300, 120)

        val menuSelectQuestClick = Location(2290, 440)
        val menuStartQuestClick = Location(2400, 1350)

        val menuStorySkipClick = Location(2360, 80)

        val supportScreenRegion = Region(0, 0, 200, 400)
        val supportListRegion = Region(70, 332, 378, 1091) // see docs/support_list_region.png
        val supportFriendsRegion = Region(448, 332, 1210, 1091)

        val supportMaxAscendedRegion = Region(376, 0, 16, 120)
        val supportLimitBreakRegion = Region(376, 0, 16, 90)
        val supportFriendRegion = Region(
            2234,
            supportListRegion.Y,
            120,
            supportListRegion.Height
        ) // see docs/friend_region.png

        val supportUpdateClick = Location(1670, 250)
        val supportListTopClick = Location(2480, 360)
        val supportFirstSupportClick = Location(1900, 500)

        val supportRegionToolSearchRegion = Region(2100, 0, 370, 1440)
        val supportDefaultBounds = Region(76, 0, 2356, 428)
        val supportDefaultCeBounds = Region(76, 270, 378, 150)
        val supportExtraRegion = Region(1200, 200, 130, 130)
        val supportNotFoundRegion = Region(468, 708, 100, 90)

        val battleScreenRegion = Region(2105, 1259, 336, 116) // see docs/battle_region.png

        val battleAttackClick = Location(2300, 1200)

        val battleMasterSkillOpenClick = Location(2380, 640)

        val battleBack = Location(2400, 1370)

        val resultScreenRegion = Region(100, 300, 700, 200)
        val resultBondRegion = Region(2000, 750, 120, 190)
        val resultMasterExpRegion = Region(1280, 350, 400, 110)
        val resultMatRewardsRegion = Region(2080, 1220, 280, 200)
        val resultMasterLvlUpRegion = Region(1990, 160, 250, 270)

        val resultCeRewardRegion = Region(1050, 1216, 33, 28)
        val resultCeRewardDetailsRegion = Region(0, 512, 135, 115)
        val resultCeRewardCloseClick = Location(80, 60)

        val resultFriendRequestRegion = Region(1880, 150, 100, 94)
        val resultFriendRequestRejectClick = Location(600, 1200)
        val resultQuestRewardRegion = Region(1630, 140, 370, 250)
        val resultClick = Location(1600, 1350)
        val resultNextClick = Location(2200, 1350) // see docs/quest_result_next_click.png
        val resultDropScrollbarRegion = Region(2260, 230, 100, 88)

        val gudaFinalRewardsRegion = Region(1160, 1040, 228, 76)
        val friendPtSummonCheck = Region(1380, 1220, 75, 75)
        val continueSummonRegion = Region(1244, 1264, 580, 170)

        val finishedLotteryBoxRegion = Region(500, 860, 180, 100)
    }

    val scriptArea =
        Region(
            Location(),
            gameAreaManager.gameArea.size * (1 / transformationExtensions.scriptToScreenScale())
        )

    fun Location.xFromCenter() =
        this + Location(scriptArea.center.X, 0)

    fun Region.xFromCenter() =
        this + Location(scriptArea.center.X, 0)

    fun Location.xFromRight() =
        this + Location(scriptArea.right, 0)

    val continueRegion = Region(120, 1000, 800, 200).xFromCenter()
    val continueBoostClick = Location(-20, 1120).xFromCenter()
    val continueClick = Location(370, 1120).xFromCenter()

    val inventoryFullRegion = Region(-230, 900, 458, 90).xFromCenter()

    val menuStorySkipYesClick = Location(320, 1100).xFromCenter()

    val retryRegion = Region(20, 1000, 700, 300).xFromCenter()

    val staminaScreenRegion = Region(-680, 200, 300, 300).xFromCenter()
    val staminaOkClick = Location(370, 1120).xFromCenter()

    val withdrawRegion = Region(-880, 540, 1800, 190).xFromCenter()
    val withdrawAcceptClick = Location(485, 720).xFromCenter()
    val withdrawCloseClick = Location(-10, 1140).xFromCenter()

    val battleSkillOkClick = Location(400, 850).xFromCenter()

    val battleOrderChangeOkClick = Location(0, 1260).xFromCenter()

    val battleExtraInfoWindowCloseClick = Location(-10, 10).xFromRight()

    val supportUpdateYesClick = Location(200, 1110).xFromCenter()

    fun locate(refillResource: RefillResourceEnum) = when (refillResource) {
        RefillResourceEnum.Bronze -> 1140
        RefillResourceEnum.Silver -> 922
        RefillResourceEnum.Gold -> 634
        RefillResourceEnum.SQ -> 345
    }.let { y -> Location(-530, y).xFromCenter() }

    val selectedPartyRegion = Region(-270, 62, 550, 72).xFromCenter()
    val partySelectionArray = (0..9).map {
        // Party indicators are center-aligned
        val x = ((it - 4.5) * 50).roundToInt()

        Location(x, 100).xFromCenter()
    }

    val battleStageCountRegion
        get() = when (prefs.gameServer) {
            GameServerEnum.Tw -> Region(1710, 25, 55, 60)
            GameServerEnum.Jp -> Region(1764, 28, 31, 44)
            else -> Region(1722, 25, 46, 53)
        }
}
