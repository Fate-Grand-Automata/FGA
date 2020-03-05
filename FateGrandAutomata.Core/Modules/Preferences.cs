namespace FateGrandAutomata
{
    public static class Preferences
    {
        public static bool SkillConfirmation { get; set; }

        public static bool EnableAutoSkill { get; set; }

        public static string SkillCommand { get; set; } = "";

        public static string BattleCardPriority { get; set; } = "BAQ";

        public static BattleNoblePhantasmType BattleNoblePhantasm { get; set; }

        public static bool BattleAutoChooseTarget { get; set; }

        public static bool UnstableFastSkipDeadAnimation { get; set; }

        public static class Support
        {
            public static string FriendNames { get; set; }

            public static string PreferredServants { get; set; }

            public static string PreferredCEs { get; set; }

            public static bool FriendsOnly { get; set; }

            public static int SwipesPerUpdate { get; set; } = 10;

            public static int MaxUpdates { get; set; } = 3;

            public static SupportSelectionMode SelectionMode { get; set; }

            public static SupportSelectionMode FallbackTo { get; set; }
        }
    }
}