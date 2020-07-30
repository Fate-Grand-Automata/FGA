package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.seconds

fun IFGAutomataApi.needsToRetry() = game.retryRegion.exists(images.retry)

fun IFGAutomataApi.retry() {
    game.retryRegion.click()

    2.seconds.wait()
}

@ScriptScope
class Game @Inject constructor(val prefs: IPreferences) {
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

    // see docs/menu_boost_item_click_array.png
    val menuBoostItem1Click = Location(1280, 418)
    val menuBoostItem2Click = Location(1280, 726)
    val menuBoostItem3Click = Location(1280, 1000)
    val menuBoostItemSkipClick = Location(1652, 1304)

    val menuBoostItemClickArray = listOf(
        menuBoostItemSkipClick,
        menuBoostItem1Click,
        menuBoostItem2Click,
        menuBoostItem3Click
    )

    val inventoryFullRegion = Region(1050, 900, 458, 90)

    val staminaScreenRegion = Region(600, 200, 300, 300)
    val staminaOkClick = Location(1650, 1120)
    val staminaSqClick = Location(1270, 345)
    val staminaGoldClick = Location(1270, 634)
    val staminaSilverClick = Location(1270, 922)
    val staminaBronzeClick = Location(1270, 1140)

    val supportScreenRegion = Region(0, 0, 200, 400)
    val supportListRegion = Region(70, 332, 378, 1091) // see docs/support_list_region.png
    val supportSwipeStartClick = Location(35, 1190)
    val supportSwipeEndClick = Location(5, 660)
    val supportFriendsRegion = Region(448, 332, 1210, 1091)

    val supportListItemRegionArray = listOf(
        // see docs/support_list_item_regions_top.png
        Region(76, 338, 2356, 428),
        Region(76, 778, 2356, 390),
        // see docs/support_list_item_regions_bottom.png
        Region(76, 558, 2356, 390),
        Region(76, 991, 2356, 428)
    )

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

    val battleStageCountRegion
        get() = when (prefs.gameServer) {
            GameServerEnum.En -> Region(1722, 25, 46, 53)
            GameServerEnum.Jp -> Region(1722, 25, 46, 53)
            GameServerEnum.Cn -> Region(1722, 25, 46, 53)
            GameServerEnum.Tw -> Region(1710, 25, 55, 60)
        }

    val battleExtraInfoWindowCloseClick = Location(2550, 10)
    val battleAttackClick = Location(2300, 1200)

    // see docs/target_regions.png
    val battleTargetRegionArray = listOf(
        Region(0, 0, 485, 220),
        Region(485, 0, 482, 220),
        Region(967, 0, 476, 220)
    )

    val battleTargetClickArray = listOf(
        Location(90, 80),
        Location(570, 80),
        Location(1050, 80)
    )

    val battleSkill1Click = Location(140, 1160)
    val battleSkill2Click = Location(340, 1160)
    val battleSkill3Click = Location(540, 1160)
    val battleSkill4Click = Location(770, 1160)
    val battleSkill5Click = Location(970, 1160)
    val battleSkill6Click = Location(1140, 1160)
    val battleSkill7Click = Location(1400, 1160)
    val battleSkill8Click = Location(1600, 1160)
    val battleSkill9Click = Location(1800, 1160)
    val battleSkillOkClick = Location(1680, 850)

    val battleServant1Click = Location(700, 880)
    val battleServant2Click = Location(1280, 880)
    val battleServant3Click = Location(1940, 880)

    val battleMasterSkillOpenClick = Location(2380, 640)
    val battleMasterSkill1Click = Location(1820, 620)
    val battleMasterSkill2Click = Location(2000, 620)
    val battleMasterSkill3Click = Location(2160, 620)

    val battleStartingMember1Click = Location(280, 700)
    val battleStartingMember2Click = Location(680, 700)
    val battleStartingMember3Click = Location(1080, 700)
    val battleSubMember1Click = Location(1480, 700)
    val battleSubMember2Click = Location(1880, 700)
    val battleSubMember3Click = Location(2280, 700)
    val battleOrderChangeOkClick = Location(1280, 1260)

    val battleCardAffinityRegionArray = listOf(
        // see docs/card_affinity_regions.png
        Region(295, 650, 250, 200),
        Region(810, 650, 250, 200),
        Region(1321, 650, 250, 200),
        Region(1834, 650, 250, 200),
        Region(2348, 650, 250, 200)
    )

    val battleCardTypeRegionArray = listOf(
        // see docs/card_type_regions.png
        Region(0, 1060, 512, 200),
        Region(512, 1060, 512, 200),
        Region(1024, 1060, 512, 200),
        Region(1536, 1060, 512, 200),
        Region(2048, 1060, 512, 200)
    )

    val battleCommandCardClickArray = listOf(
        Location(300, 1000),
        Location(750, 1000),
        Location(1300, 1000),
        Location(1800, 1000),
        Location(2350, 1000)
    )

    val battleNpCardClickArray = listOf(
        Location(1000, 220),
        Location(1300, 400),
        Location(1740, 400)
    )

    val resultScreenRegion = Region(100, 300, 700, 200)
    val resultBondRegion = Region(2000, 750, 120, 190)
    val resultMasterExpRegion = Region(1280, 350, 400, 110)
    val resultMatRewardsRegion = Region(2080, 1290, 280, 130)
    val resultMasterLvlUpRegion = Region(1990, 160, 250, 270)

    val resultCeRewardRegion = Region(1050, 1216, 33, 28)
    val resultCeRewardCloseClick = Location(80, 60)
    val resultFriendRequestRegion = Region(660, 120, 140, 160)
    val resultFriendRequestRejectClick = Location(600, 1200)
    val resultQuestRewardRegion = Region(1630, 140, 370, 250)
    val resultNextClick = Location(2200, 1350) // see docs/quest_result_next_click.png

    val gudaFinalRewardsRegion = Region(1160, 1040, 228, 76)
}

object BattleServantCards {
    val faceCardRegions = listOf(
        Region(106, 800, 300, 200),
        Region(620, 800, 300, 200),
        Region(1130, 800, 300, 200),
        Region(1644, 800, 300, 200),
        Region(2160, 800, 300, 200)
    )

    val npRegions = listOf(
        Region(678, 190, 300, 200),
        Region(1138, 190, 300, 200),
        Region(1606, 190, 300, 200)
    )

    val faceCardCropRegions = listOf(
        Region(200, 890, 115, 85),
        Region(714, 890, 115, 85),
        Region(1224, 890, 115, 85),
        Region(1738, 890, 115, 85),
        Region(2254, 890, 115, 85)
    )

    val npCropRegions = listOf(
        Region(762, 290, 115, 65),
        Region(1230, 290, 115, 65),
        Region(1694, 290, 115, 65)
    )
}

fun IFGAutomataApi.groupNpsWithFaceCards(groups: List<List<Int>>): List<List<Int>> {
    return BattleServantCards.npCropRegions
        .map { region ->
            region.getPattern().use { npCropped ->
                groups.maxBy {
                    BattleServantCards.faceCardRegions[it[0]]
                        .findAll(npCropped, 0.4)
                        .firstOrNull()?.score ?: 0.0
                } ?: emptyList()
            }
        }
}

fun IFGAutomataApi.groupByFaceCard(): List<List<Int>> {
    val remaining = BattleServantCards.faceCardRegions.indices.toMutableSet()
    val groups = mutableListOf<List<Int>>()

    while (remaining.isNotEmpty()) {
        val u = remaining.first()
        remaining.remove(u)

        val group = mutableListOf<Int>()
        group.add(u)

        if (remaining.isNotEmpty()) {
            val me = BattleServantCards.faceCardCropRegions[u].getPattern()

            me.use {
                val matched = remaining.filter {
                    val region = BattleServantCards.faceCardRegions[it]
                    region.exists(me)
                }

                remaining.removeAll(matched)
                group.addAll(matched)
            }
        }

        groups.add(group)
    }

    return groups
}