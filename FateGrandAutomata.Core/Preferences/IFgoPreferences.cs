namespace FateGrandAutomata
{
    public interface IFgoPreferences
    {
        GameServer GameServer { get; }

        bool SkillConfirmation { get; }

        bool EnableAutoSkill { get; }

        string SkillCommand { get; }

        string BattleCardPriority { get; }

        BattleNoblePhantasmType BattleNoblePhantasm { get; }

        bool BattleAutoChooseTarget { get; }

        bool UnstableFastSkipDeadAnimation { get; }

        bool StorySkip { get; }

        bool StopAfterBond10 { get; }

        bool WithdrawEnabled { get; }

        bool DebugMode { get; }

        /// <summary>
        /// 0 (skip) - 3
        /// </summary>
        int BoostItemSelectionMode { get; }

        IFgoSupportPreferences Support { get; }

        IFgoRefillPreferences Refill { get; }
    }
}