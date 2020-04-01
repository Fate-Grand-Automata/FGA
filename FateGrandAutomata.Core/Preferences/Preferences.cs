using CoreAutomata;

namespace FateGrandAutomata
{
    public class Preferences : PropertyStore
    {
        Preferences() { }

        public static Preferences Instance { get; } = new Preferences();

        public GameServer GameServer { get; set; }

        public bool SkillConfirmation { get; set; }

        public bool EnableAutoSkill { get; set; } = true;

        public string SkillCommand { get; set; } = "4,#,f5,#,i6";

        public string BattleCardPriority { get; set; } = "BAQ";

        public BattleNoblePhantasmType BattleNoblePhantasm { get; set; }

        public bool BattleAutoChooseTarget { get; set; }

        public bool UnstableFastSkipDeadAnimation { get; set; }

        public bool StorySkip { get; set; }

        public bool StopAfterBond10 { get; set; }

        /// <summary>
        /// 0 (skip) - 3
        /// </summary>
        public int BoostItemSelectionMode { get; set; }

        public SupportPreferences Support { get; } = new SupportPreferences();

        public RefillPreferences Refill { get; } = new RefillPreferences();
    }
}