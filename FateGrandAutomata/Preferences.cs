namespace FateGrandAutomata
{
    public static class Preferences
    {
        public static bool SkillConfirmation { get; set; }

        public static bool EnableAutoSkill { get; set; }

        public static string SkillCommand { get; set; } = "";

        public static string BattleCardPriority { get; set; } = "BAQ";

        public static BattleNoblePhantasmType BattleNoblePhantasm { get; set; }
    }
}