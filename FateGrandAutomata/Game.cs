using System;
using System.Collections.Generic;
using System.Threading;

namespace FateGrandAutomata
{
    public static class Game
    {
        public const string GeneralImagePath = "images/";

        public static void Wait(double Seconds)
        {
            Thread.Sleep(TimeSpan.FromSeconds(Seconds));
        }

        public static void Toast(string Msg) => throw new NotImplementedException();

        public static Region MenuScreenRegion { get; } = new Region(2100, 1200, 1000, 1000);
        public static Region ContinueRegion { get; } = new Region(1400, 1000, 600, 200);
        public static Region MenuStorySkipRegion { get; } = new Region(2240, 20, 300, 120);

        public static Point MenuSelectQuestClick { get; } = new Point(1900, 400);
        public static Point MenuStartQuestClick { get; } = new Point(2400, 1350);
        public static Point ContinueClick { get; } = new Point(1650, 1120);
        public static Point MenuStorySkipClick { get; } = new Point(2360, 80);
        public static Point MenuStorySkipYesClick { get; } = new Point(1600, 1100);

        // see docs/menu_boost_item_click_array.png
        public static Point MenuBoostItem1Click { get; } = new Point(1280, 418);
        public static Point MenuBoostItem2Click { get; } = new Point(1280, 726);
        public static Point MenuBoostItem3Click { get; } = new Point(1280, 1000);
        public static Point MenuBoostItemSkipClick { get; } = new Point(1652, 1304);

        public static Dictionary<string, Point> MenuBoostItemClickArray { get;  } = new Dictionary<string, Point>
        {
            ["1"] = MenuBoostItem1Click,
            ["2"] = MenuBoostItem2Click,
            ["3"] = MenuBoostItem3Click,
            ["disabled"] = MenuBoostItemSkipClick
        };

        public static Region StaminaScreenRegion { get; } = new Region(600, 200, 300, 300);
        public static Point StaminaOkClick { get; } = new Point(1650, 1120);
        public static Point StaminaSqClick { get; } = new Point(1270, 345);
        public static Point StaminaGoldClick { get; } = new Point(1270, 634);
        public static Point StaminaSilverClick { get; } = new Point(1270, 922);
        public static Point StaminaBronzeClick { get; } = new Point(1270, 1140);

        public static Region SupportScreenRegion { get; } = new Region(0, 0, 110, 332);
        public static Region SupportListRegion { get; } = new Region(70, 332, 378, 1091); // see docs/support_list_region.png
        public static Point SupportSwipeStartClick { get; } = new Point(35, 1190);
        public static Region SupportFriendsRegion { get; } = new Region(448, 332, 1210, 1091);

        // TODO: Different for each server
        public static Region SupportSwipeEndClick => throw new NotImplementedException();

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

        public static Point SupportUpdateClick { get; } = new Point(1670, 250);
        public static Point SupportUpdateYesClick { get; } = new Point(1480, 1110);
        public static Point SupportListTopClick { get; } = new Point(2480, 360);
        public static Point SupportFirstSupportClick { get; } = new Point(1900, 500);

        public static Region BattleScreenRegion = new Region(2105, 1259, 336, 116); // see docs/battle_region.png

        // TODO: Different for each server
        public static Region BattleStageCountRegion => throw new NotImplementedException();

        public static Point BattleExtrainfoWindowCloseClick { get; } = new Point(2550, 0);
        public static Point BattleAttackClick { get; } = new Point(2300, 1200);
        public static Point BattleSkipDeathAnimationClick { get; } = new Point(1700, 100); // see docs/skip_death_animation_click.png

        // see docs/target_regions.png
        public static Region[] BattleTargetRegionArray { get; } =
        {
            new Region(0, 0, 485, 220),
            new Region(485, 0, 482, 220),
            new Region(967, 0, 476, 220)
        };

        public static Point[] BattleTargetClickArray { get; } =
        {
            new Point(90, 80),
            new Point(570, 80),
            new Point(1050, 80)
        };

        public static Point BattleSkill1Click { get; } = new Point(140, 1160);
        public static Point BattleSkill2Click { get; } = new Point(340, 1160);
        public static Point BattleSkill3Click { get; } = new Point(540, 1160);
        public static Point BattleSkill4Click { get; } = new Point(770, 1160);
        public static Point BattleSkill5Click { get; } = new Point(970, 1160);
        public static Point BattleSkill6Click { get; } = new Point(1140, 1160);
        public static Point BattleSkill7Click { get; } = new Point(1400, 1160);
        public static Point BattleSkill8Click { get; } = new Point(1600, 1160);
        public static Point BattleSkill9Click { get; } = new Point(1800, 1160);
        public static Point BattleSkillOkClick { get; } = new Point(1680, 850);

        public static Point BattleServant1Click { get; } = new Point(700, 880);
        public static Point BattleServant2Click { get; } = new Point(1280, 880);
        public static Point BattleServant3Click { get; } = new Point(1940, 880);

        public static Point BattleMasterSkillOpenClick { get; } = new Point(2380, 640);
        public static Point BattleMasterSkill1Click { get; } = new Point(1820, 620);
        public static Point BattleMasterSkill2Click { get; } = new Point(2000, 620);
        public static Point BattleMasterSkill3Click { get; } = new Point(2160, 620);

        public static Point BattleStartingMember1Click { get; } = new Point(280, 700);
        public static Point BattleStartingMember2Click { get; } = new Point(680, 700);
        public static Point BattleStartingMember3Click { get; } = new Point(1080, 700);
        public static Point BattleSubMember1Click { get; } = new Point(1480, 700);
        public static Point BattleSubMember2Click { get; } = new Point(1880, 700);
        public static Point BattleSubMember3Click { get; } = new Point(2280, 700);
        public static Point BattleOrderChangeOkClick { get; } = new Point(1280, 1260);

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

        public static Point[] BattleCommandCardClickArray { get; } = {
            new Point(300, 1000),
            new Point(750, 1000),
            new Point(1300, 1000),
            new Point(1800, 1000),
            new Point(2350, 1000),
        };

        public static Point[] BattleNpCardClickArray { get; } = {
            new Point(1000, 220),
            new Point(1300, 400),
            new Point(1740, 400)
        };

        public static Region ResultScreenRegion { get; } = new Region(100, 300, 700, 200);
        public static Region ResultBondRegion { get; } = new Region(2000, 820, 120, 120);
        public static Region ResultCeRewardRegion { get; } = new Region(1050, 1216, 33, 28);
        public static Point ResultCeRewardCloseClick { get; } = new Point(80, 60);
        public static Region ResultFriendRequestRegion { get; } = new Region(660, 120, 140, 160);
        public static Point ResultFriendRequestRejectClick { get; } = new Point(600, 1200);
        public static Region ResultQuestRewardRegion { get; } = new Region(1630, 140, 370, 250);
        public static Point ResultNextClick { get; } = new Point(2200, 1350); // see docs/quest_result_next_click.png
    }
}