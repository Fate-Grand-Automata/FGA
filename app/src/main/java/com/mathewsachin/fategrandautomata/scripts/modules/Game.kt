package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlin.time.seconds

class Game {
    companion object {
        val ImageSize = Size(1280, 720)
        val ScriptSize = Size(2560, 1440)

        val MenuScreenRegion = Region(2100, 1200, 1000, 1000)
        val ContinueRegion = Region(1400, 1000, 600, 200)
        val MenuStorySkipRegion = Region(2240, 20, 300, 120)

        val MenuSelectQuestClick = Location(2290, 440)
        val MenuStartQuestClick = Location(2400, 1350)
        val ContinueClick = Location(1650, 1120)
        val MenuStorySkipClick = Location(2360, 80)
        val MenuStorySkipYesClick = Location(1600, 1100)

        val RetryRegion = Region(1300, 1000, 700, 300)
        val WithdrawRegion = Region(400, 540, 1800, 190)
        val WithdrawAcceptClick = Location(1765, 720)

        // see docs/menu_boost_item_click_array.png
        val MenuBoostItem1Click = Location(1280, 418)
        val MenuBoostItem2Click = Location(1280, 726)
        val MenuBoostItem3Click = Location(1280, 1000)
        val MenuBoostItemSkipClick = Location(1652, 1304)

        val MenuBoostItemClickArray = listOf(
            MenuBoostItemSkipClick,
            MenuBoostItem1Click,
            MenuBoostItem2Click,
            MenuBoostItem3Click
        )

        val InventoryFullRegion = Region(1060, 910, 438, 70)

        val StaminaScreenRegion = Region(600, 200, 300, 300)
        val StaminaOkClick = Location(1650, 1120)
        val StaminaSqClick = Location(1270, 345)
        val StaminaGoldClick = Location(1270, 634)
        val StaminaSilverClick = Location(1270, 922)
        val StaminaBronzeClick = Location(1270, 1140)

        val SupportScreenRegion = Region(0, 0, 200, 400)
        val SupportListRegion = Region(70, 332, 378, 1091) // see docs/support_list_region.png
        val SupportSwipeStartClick = Location(35, 1190)
        val SupportSwipeEndClick = Location(5, 660)
        val SupportFriendsRegion = Region(448, 332, 1210, 1091)

        val SupportListItemRegionArray = listOf(
            // see docs/support_list_item_regions_top.png
            Region(76, 338, 2356, 428),
            Region(76, 778, 2356, 390),
            // see docs/support_list_item_regions_bottom.png
            Region(76, 558, 2356, 390),
            Region(76, 991, 2356, 428)
        )

        val SupportLimitBreakRegion = Region(376, 0, 16, 90)
        val SupportFriendRegion = Region(
            2234,
            SupportListRegion.Y,
            120,
            SupportListRegion.Height
        ) // see docs/friend_region.png

        val SupportUpdateClick = Location(1670, 250)
        val SupportUpdateYesClick = Location(1480, 1110)
        val SupportListTopClick = Location(2480, 360)
        val SupportFirstSupportClick = Location(1900, 500)

        val BattleScreenRegion = Region(2105, 1259, 336, 116) // see docs/battle_region.png

        val BattleStageCountRegion
            get() = when (Preferences.GameServer) {
                GameServerEnum.En -> Region(1722, 25, 46, 53)
                GameServerEnum.Jp -> Region(1722, 25, 46, 53)
                GameServerEnum.Cn -> Region(1722, 25, 46, 53)
                GameServerEnum.Tw -> Region(1710, 25, 55, 60)
            }

        val BattleExtrainfoWindowCloseClick = Location(2550, 10)
        val BattleAttackClick = Location(2300, 1200)

        // see docs/target_regions.png
        val BattleTargetRegionArray = listOf(
            Region(0, 0, 485, 220),
            Region(485, 0, 482, 220),
            Region(967, 0, 476, 220)
        )

        val BattleTargetClickArray = listOf(
            Location(90, 80),
            Location(570, 80),
            Location(1050, 80)
        )

        val BattleSkill1Click = Location(140, 1160)
        val BattleSkill2Click = Location(340, 1160)
        val BattleSkill3Click = Location(540, 1160)
        val BattleSkill4Click = Location(770, 1160)
        val BattleSkill5Click = Location(970, 1160)
        val BattleSkill6Click = Location(1140, 1160)
        val BattleSkill7Click = Location(1400, 1160)
        val BattleSkill8Click = Location(1600, 1160)
        val BattleSkill9Click = Location(1800, 1160)
        val BattleSkillOkClick = Location(1680, 850)

        val BattleServant1Click = Location(700, 880)
        val BattleServant2Click = Location(1280, 880)
        val BattleServant3Click = Location(1940, 880)

        val BattleMasterSkillOpenClick = Location(2380, 640)
        val BattleMasterSkill1Click = Location(1820, 620)
        val BattleMasterSkill2Click = Location(2000, 620)
        val BattleMasterSkill3Click = Location(2160, 620)

        val BattleStartingMember1Click = Location(280, 700)
        val BattleStartingMember2Click = Location(680, 700)
        val BattleStartingMember3Click = Location(1080, 700)
        val BattleSubMember1Click = Location(1480, 700)
        val BattleSubMember2Click = Location(1880, 700)
        val BattleSubMember3Click = Location(2280, 700)
        val BattleOrderChangeOkClick = Location(1280, 1260)

        val BattleCardAffinityRegionArray = listOf(
            // see docs/card_affinity_regions.png
            Region(295, 650, 250, 200),
            Region(810, 650, 250, 200),
            Region(1321, 650, 250, 200),
            Region(1834, 650, 250, 200),
            Region(2348, 650, 250, 200)
        )

        val BattleCardTypeRegionArray = listOf(
            // see docs/card_type_regions.png
            Region(0, 1060, 512, 200),
            Region(512, 1060, 512, 200),
            Region(1024, 1060, 512, 200),
            Region(1536, 1060, 512, 200),
            Region(2048, 1060, 512, 200)
        )

        val BattleCommandCardClickArray = listOf(
            Location(300, 1000),
            Location(750, 1000),
            Location(1300, 1000),
            Location(1800, 1000),
            Location(2350, 1000)
        )

        val BattleNpCardClickArray = listOf(
            Location(1000, 220),
            Location(1300, 400),
            Location(1740, 400)
        )

        val BattleServantFaceRegionArray = listOf(
            Region(106, 800, 300, 200),
            Region(620, 800, 300, 200),
            Region(1130, 800, 300, 200),
            Region(1644, 800, 300, 200),
            Region(2160, 800, 300, 200),

            Region(678, 190, 300, 200),
            Region(1138, 190, 300, 200),
            Region(1606, 190, 300, 200)
        )

        val ResultScreenRegion = Region(100, 300, 700, 200)
        val ResultBondRegion = Region(2000, 750, 120, 190)
        val ResultMasterExpRegion = Region(1290, 360, 380, 90)
        val ResultMatRewardsRegion = Region(2090, 1300, 260, 110)
        val ResultMasterLvlUpRegion = Region(2000, 170, 230, 250)

        val ResultCeRewardRegion = Region(1050, 1216, 33, 28)
        val ResultCeRewardCloseClick = Location(80, 60)
        val ResultFriendRequestRegion = Region(660, 120, 140, 160)
        val ResultFriendRequestRejectClick = Location(600, 1200)
        val ResultQuestRewardRegion = Region(1630, 140, 370, 250)
        val ResultNextClick = Location(2200, 1350) // see docs/quest_result_next_click.png

        fun needsToRetry() = RetryRegion.exists(ImageLocator.Retry)

        fun retry() {
            RetryRegion.click()

            AutomataApi.wait(2.seconds)
        }

        val GudaFinalRewardsRegion = Region(1160, 1040, 228, 76)
    }
}