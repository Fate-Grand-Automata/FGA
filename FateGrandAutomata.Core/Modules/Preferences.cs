namespace FateGrandAutomata
{
    public static class Preferences
    {
        public static GameServer GameServer { get; set; }

        public static bool SkillConfirmation { get; set; }

        public static bool EnableAutoSkill { get; set; } = true;

        public static string SkillCommand { get; set; } = "4,#,f5,#,i6";

        public static string BattleCardPriority { get; set; } = "BAQ";

        public static BattleNoblePhantasmType BattleNoblePhantasm { get; set; }

        public static bool BattleAutoChooseTarget { get; set; }

        public static bool UnstableFastSkipDeadAnimation { get; set; }

        public static bool StorySkip { get; set; }

        public static bool StopAfterBond10 { get; set; }

        /// <summary>
        /// 0 (skip) - 3
        /// </summary>
        public static int BoostItemSelectionMode { get; set; }

        public static class Support
        {
            public static string FriendNames { get; set; }

            public static string PreferredServants { get; set; }

            public static string PreferredCEs { get; set; } = "*mona_lisa.png";

            public static bool FriendsOnly { get; set; }

            public static int SwipesPerUpdate { get; set; } = 10;

            public static int MaxUpdates { get; set; } = 3;

            public static SupportSelectionMode SelectionMode { get; set; } = SupportSelectionMode.Preferred;

            public static SupportSelectionMode FallbackTo { get; set; } = SupportSelectionMode.First;
        }

        public static class Refill
        {
            public static bool Enabled { get; set; }

            public static int Repetitions { get; set; }

            public static RefillResource Resource { get; set; }
        }
    }
}