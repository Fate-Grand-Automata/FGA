namespace FateGrandAutomata
{
    public interface IFgoPreferences
    {
        ScriptMode ScriptMode { get; }

        GameServer GameServer { get; }

        bool SkillConfirmation { get; }

        bool EnableAutoSkill { get; }

        string SkillCommand { get; }

        string BattleCardPriority { get; }

        BattleNoblePhantasmType BattleNoblePhantasm { get; }

        bool BattleAutoChooseTarget { get; }

        bool StorySkip { get; }

        bool StopAfterBond10 { get; }

        bool WithdrawEnabled { get; }

        /// <summary>
        /// -ve = disabled, 0 = skip, 1-3 = items
        /// </summary>
        int BoostItemSelectionMode { get; }

        IFgoSupportPreferences Support { get; }

        IFgoRefillPreferences Refill { get; }

        bool IgnoreNotchCalculation { get; }

        bool UseRootForScreenshots { get; }

        bool UseRootForGestures { get; }

        bool GudaFinal { get; }
    }
}