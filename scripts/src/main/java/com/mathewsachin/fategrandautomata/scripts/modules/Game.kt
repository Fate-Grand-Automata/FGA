package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.seconds

fun IFgoAutomataApi.needsToRetry() = images.retry in Game.retryRegion

fun IFgoAutomataApi.retry() {
    Game.retryRegion.click()

    2.seconds.wait()
}

@ScriptScope
class Game @Inject constructor(val prefs: IPreferences) {
    companion object {
        val imageSize = Size(1280, 720)
        val scriptSize = Size(2560, 1440)

        val menuScreenRegion = Region(2100, 1200, 1000, 1000)
        val continueRegion = Region(1400, 1000, 600, 200)
        val menuStorySkipRegion = Region(2240, 20, 300, 120)

        val menuSelectQuestClick = Location(2290, 440)
        val menuStartQuestClick = Location(2400, 1350)
        val continueClick = Location(1650, 1120)
        val menuStorySkipClick = Location(2360, 80)
        val menuStorySkipYesClick = Location(1600, 1100)

        val retryRegion = Region(1300, 1000, 700, 300)
        val withdrawRegion = Region(400, 540, 1800, 190)
        val withdrawAcceptClick = Location(1765, 720)
        val withdrawCloseClick = Location(1270, 1140)

        val inventoryFullRegion = Region(1050, 900, 458, 90)

        val staminaScreenRegion = Region(600, 200, 300, 300)
        val staminaOkClick = Location(1650, 1120)

        val supportScreenRegion = Region(0, 0, 200, 400)
        val supportListRegion = Region(70, 332, 378, 1091) // see docs/support_list_region.png
        val supportSwipeStartClick = Location(35, 1190)
        val supportSwipeEndClick = Location(5, 660)
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
        val supportUpdateYesClick = Location(1480, 1110)
        val supportListTopClick = Location(2480, 360)
        val supportFirstSupportClick = Location(1900, 500)

        val supportRegionToolSearchRegion = Region(2100, 0, 370, 1440)
        val supportDefaultBounds = Region(76, 0, 2356, 428)
        val supportDefaultCeBounds = Region(76, 270, 378, 150)
        val supportExtraRegion = Region(1200, 200, 130, 130)
        val supportNotFoundRegion = Region(468, 708, 100, 90)

        val selectedPartyRegion = Region(1010, 62, 550, 72)
        val partySelectionArray = listOf(
            Location(1055, 100),
            Location(1105, 100),
            Location(1155, 100),
            Location(1205, 100),
            Location(1255, 100),
            Location(1305, 100),
            Location(1355, 100),
            Location(1405, 100),
            Location(1455, 100),
            Location(1505, 100)
        )

        val battleScreenRegion = Region(2105, 1259, 336, 116) // see docs/battle_region.png

        val battleExtraInfoWindowCloseClick = Location(2550, 10)
        val battleAttackClick = Location(2300, 1200)
        val battleSkillOkClick = Location(1680, 850)

        val battleMasterSkillOpenClick = Location(2380, 640)
        val battleOrderChangeOkClick = Location(1280, 1260)

        val resultScreenRegion = Region(100, 300, 700, 200)
        val resultBondRegion = Region(2000, 750, 120, 190)
        val resultMasterExpRegion = Region(1280, 350, 400, 110)
        val resultMatRewardsRegion = Region(2080, 1290, 280, 130)
        val resultMasterLvlUpRegion = Region(1990, 160, 250, 270)

        val resultCeDropRegion = Region(1860, 0, 240, 100)
        val resultCeRewardRegion = Region(1050, 1216, 33, 28)
        val resultCeRewardDetailsRegion = Region(310, 1295, 45, 30)
        val resultCeRewardCloseClick = Location(80, 60)
        val resultFriendRequestRegion = Region(660, 120, 140, 160)
        val resultFriendRequestRejectClick = Location(600, 1200)
        val resultQuestRewardRegion = Region(1630, 140, 370, 250)
        val resultClick = Location(1600, 1350)
        val resultNextClick = Location(2200, 1350) // see docs/quest_result_next_click.png
        val resultDropScrollbarRegion = Region(2260, 230, 100, 88)

        val gudaFinalRewardsRegion = Region(1160, 1040, 228, 76)
        val friendPtSummonCheck = Region(1380, 1220, 75, 75)
        val continueSummonRegion = Region(1244, 1264, 580, 170)
    }

    val battleStageCountRegion
        get() = when (prefs.gameServer) {
            GameServerEnum.Tw -> Region(1710, 25, 55, 60)
            else -> Region(1722, 25, 46, 53)
        }
}