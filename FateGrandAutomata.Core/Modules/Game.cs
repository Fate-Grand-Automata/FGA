using CoreAutomata;

namespace FateGrandAutomata
{
    public static class Game
    {
        public const int ImageWidth = 1280,
            ImageHeight = 720,
            ScriptWidth = 2560,
            ScriptHeight = 1440;

        public static Region MenuScreenRegion { get; } = new Region(2100, 1200, 1000, 1000);
        public static Region ContinueRegion { get; } = new Region(1400, 1000, 600, 200);
        public static Region MenuStorySkipRegion { get; } = new Region(2240, 20, 300, 120);

        public static Location MenuSelectQuestClick { get; } = new Location(1900, 400);
        public static Location MenuStartQuestClick { get; } = new Location(2400, 1350);
        public static Location ContinueClick { get; } = new Location(1650, 1120);
        public static Location MenuStorySkipClick { get; } = new Location(2360, 80);
        public static Location MenuStorySkipYesClick { get; } = new Location(1600, 1100);

        // see docs/menu_boost_item_click_array.png
        public static Location MenuBoostItem1Click { get; } = new Location(1280, 418);
        public static Location MenuBoostItem2Click { get; } = new Location(1280, 726);
        public static Location MenuBoostItem3Click { get; } = new Location(1280, 1000);
        public static Location MenuBoostItemSkipClick { get; } = new Location(1652, 1304);

        public static Location[] MenuBoostItemClickArray { get;  } = new[]
        {
            MenuBoostItemSkipClick,
            MenuBoostItem1Click,
            MenuBoostItem2Click,
            MenuBoostItem3Click
        };

        public static Region StaminaScreenRegion { get; } = new Region(600, 200, 300, 300);
        public static Location StaminaOkClick { get; } = new Location(1650, 1120);
        public static Location StaminaSqClick { get; } = new Location(1270, 345);
        public static Location StaminaGoldClick { get; } = new Location(1270, 634);
        public static Location StaminaSilverClick { get; } = new Location(1270, 922);
        public static Location StaminaBronzeClick { get; } = new Location(1270, 1140);

        public static Region SupportScreenRegion { get; } = new Region(0, 0, 110, 332);
        public static Region SupportListRegion { get; } = new Region(70, 332, 378, 1091); // see docs/support_list_region.png
        public static Location SupportSwipeStartClick { get; } = new Location(35, 1190);
        public static Region SupportFriendsRegion { get; } = new Region(448, 332, 1210, 1091);

        public static Location SupportSwipeEndClick => Preferences.Instance.GameServer switch
        {
            GameServer.En => new Location(35, 390),
            GameServer.Jp => new Location(35, 350),
            GameServer.Cn => new Location(35, 390),
            GameServer.Tw => new Location(35, 390)
        };

        public static Region[] SupportListItemRegionArray { get;  } =
        {
            // see docs/support_list_item_regions_top.png
            new Region(76, 338, 2356, 428),
            new Region(76, 778, 2356, 390),

            // see docs/support_list_item_regions_bottom.png
            new Region(76, 558, 2356, 390),
            new Region(76, 991, 2356, 428)
        };

        public static Region SupportLimitBreakRegion { get; } = new Region(376, 0, 16, 90);
        public static Region SupportFriendRegion => new Region(2234, SupportListRegion.Y, 120, SupportListRegion.H); // see docs/friend_region.png

        public static Location SupportUpdateClick { get; } = new Location(1670, 250);
        public static Location SupportUpdateYesClick { get; } = new Location(1480, 1110);
        public static Location SupportListTopClick { get; } = new Location(2480, 360);
        public static Location SupportFirstSupportClick { get; } = new Location(1900, 500);

        public static Region BattleScreenRegion { get; } = new Region(2105, 1259, 336, 116); // see docs/battle_region.png

        public static Region BattleStageCountRegion => Preferences.Instance.GameServer switch
        {
            GameServer.En => new Region(1722, 25, 46, 53),
            GameServer.Jp => new Region(1722, 25, 46, 53),
            GameServer.Cn => new Region(1722, 25, 46, 53),
            GameServer.Tw => new Region(1710, 25, 55, 60)
        };

        public static Location BattleExtrainfoWindowCloseClick { get; } = new Location(2550, 0);
        public static Location BattleAttackClick { get; } = new Location(2300, 1200);
        public static Location BattleSkipDeathAnimationClick { get; } = new Location(1700, 100); // see docs/skip_death_animation_click.png

        // see docs/target_regions.png
        public static Region[] BattleTargetRegionArray { get; } =
        {
            new Region(0, 0, 485, 220),
            new Region(485, 0, 482, 220),
            new Region(967, 0, 476, 220)
        };

        public static Location[] BattleTargetClickArray { get; } =
        {
            new Location(90, 80),
            new Location(570, 80),
            new Location(1050, 80)
        };

        public static Location BattleSkill1Click { get; } = new Location(140, 1160);
        public static Location BattleSkill2Click { get; } = new Location(340, 1160);
        public static Location BattleSkill3Click { get; } = new Location(540, 1160);
        public static Location BattleSkill4Click { get; } = new Location(770, 1160);
        public static Location BattleSkill5Click { get; } = new Location(970, 1160);
        public static Location BattleSkill6Click { get; } = new Location(1140, 1160);
        public static Location BattleSkill7Click { get; } = new Location(1400, 1160);
        public static Location BattleSkill8Click { get; } = new Location(1600, 1160);
        public static Location BattleSkill9Click { get; } = new Location(1800, 1160);
        public static Location BattleSkillOkClick { get; } = new Location(1680, 850);

        public static Location BattleServant1Click { get; } = new Location(700, 880);
        public static Location BattleServant2Click { get; } = new Location(1280, 880);
        public static Location BattleServant3Click { get; } = new Location(1940, 880);

        public static Location BattleMasterSkillOpenClick { get; } = new Location(2380, 640);
        public static Location BattleMasterSkill1Click { get; } = new Location(1820, 620);
        public static Location BattleMasterSkill2Click { get; } = new Location(2000, 620);
        public static Location BattleMasterSkill3Click { get; } = new Location(2160, 620);

        public static Location BattleStartingMember1Click { get; } = new Location(280, 700);
        public static Location BattleStartingMember2Click { get; } = new Location(680, 700);
        public static Location BattleStartingMember3Click { get; } = new Location(1080, 700);
        public static Location BattleSubMember1Click { get; } = new Location(1480, 700);
        public static Location BattleSubMember2Click { get; } = new Location(1880, 700);
        public static Location BattleSubMember3Click { get; } = new Location(2280, 700);
        public static Location BattleOrderChangeOkClick { get; } = new Location(1280, 1260);

        public static Region[] BattleCardAffinityRegionArray { get; } = {
            // see docs/card_affinity_regions.png
            new Region(295, 650, 250, 200),
            new Region(810, 650, 250, 200),
            new Region(1321, 650, 250, 200),
            new Region(1834, 650, 250, 200),
            new Region(2348, 650, 250, 200)
        };

        public static Region[] BattleCardTypeRegionArray { get; } = {
            // see docs/card_type_regions.png
            new Region(0, 1060, 512, 200),
            new Region(512, 1060, 512, 200),
            new Region(1024, 1060, 512, 200),
            new Region(1536, 1060, 512, 200),
            new Region(2048, 1060, 512, 200)
        };

        public static Location[] BattleCommandCardClickArray { get; } = {
            new Location(300, 1000),
            new Location(750, 1000),
            new Location(1300, 1000),
            new Location(1800, 1000),
            new Location(2350, 1000),
        };

        public static Location[] BattleNpCardClickArray { get; } = {
            new Location(1000, 220),
            new Location(1300, 400),
            new Location(1740, 400)
        };

        public static Region ResultScreenRegion { get; } = new Region(100, 300, 700, 200);
        public static Region ResultBondRegion { get; } = new Region(2000, 820, 120, 120);
        public static Region ResultCeRewardRegion { get; } = new Region(1050, 1216, 33, 28);
        public static Location ResultCeRewardCloseClick { get; } = new Location(80, 60);
        public static Region ResultFriendRequestRegion { get; } = new Region(660, 120, 140, 160);
        public static Location ResultFriendRequestRejectClick { get; } = new Location(600, 1200);
        public static Region ResultQuestRewardRegion { get; } = new Region(1630, 140, 370, 250);
        public static Location ResultNextClick { get; } = new Location(2200, 1350); // see docs/quest_result_next_click.png
    }
}