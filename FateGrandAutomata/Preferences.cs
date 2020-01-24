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

        public static string SupportFriendNames { get; set; }

        public static string SupportPreferredServants { get; set; }
        
        public static string SupportPreferredCEs { get; set; }

        public static SupportSelectionMode SupportSelectionMode { get; set; }
    }
}