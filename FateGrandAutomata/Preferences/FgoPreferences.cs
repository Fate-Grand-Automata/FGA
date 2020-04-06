using System;
using Android.Content;
using AndroidX.Preference;
using R = FateGrandAutomata.Resource.String;

namespace FateGrandAutomata
{
    public class FgoPreferences : IFgoPreferences
    {
        readonly Context _context;
        readonly ISharedPreferences _preferences;

        public FgoPreferences(Context Context)
        {
            _context = Context;
            _preferences = PreferenceManager.GetDefaultSharedPreferences(Context);

            if (!_preferences.GetBoolean(PreferenceManager.KeyHasSetDefaultValues, false))
            {
                PreferenceManager.SetDefaultValues(Context, Resource.Xml.app_preferences, true);
                PreferenceManager.SetDefaultValues(Context, Resource.Xml.refill_preferences, true);
            }

            Refill = new FgoRefillPreferences(this);
            Support = new FgoSupportPreferences(this);
        }

        string K(int KeyId) => _context.GetString(KeyId);

        public T GetEnum<T>(int Key, T Default = default) where T: struct
        {
            return Enum.Parse<T>(_preferences.GetString(K(Key), Enum.GetName(typeof(T), Default)));
        }

        public bool GetBool(int Key, bool Default = false) => _preferences.GetBoolean(K(Key), Default);

        public string GetString(int Key, string Default = "") => _preferences.GetString(K(Key), Default);

        public int GetInt(int Key, int Default = 0)
        {
            var s = _preferences.GetString(K(Key), "");

            return int.TryParse(s, out var value) ? value : Default;
        }

        // ----- //

        public ScriptMode ScriptMode => GetEnum<ScriptMode>(R.pref_script_mode);

        public GameServer GameServer => GetEnum<GameServer>(R.pref_gameserver);

        public bool SkillConfirmation => GetBool(R.pref_skill_conf);

        public bool EnableAutoSkill => GetBool(R.pref_autoskill_enable);

        public string SkillCommand
        {
            get
            {
                var selectedAutoskillConfig = GetString(R.pref_autoskill_selected);

                if (string.IsNullOrWhiteSpace(selectedAutoskillConfig))
                    return "";

                var prefManager = _context.GetSharedPreferences(selectedAutoskillConfig, FileCreationMode.Private);

                return prefManager.GetString(_context.GetString(R.pref_autoskill_cmd), "");
            }
        }

        public string BattleCardPriority => GetString(R.pref_card_priority, "BAQ");

        public BattleNoblePhantasmType BattleNoblePhantasm => GetEnum<BattleNoblePhantasmType>(R.pref_battle_np);

        public bool BattleAutoChooseTarget => GetBool(R.pref_auto_choose_target);

        public bool UnstableFastSkipDeadAnimation => GetBool(R.pref_fast_skip_dead);

        public bool StorySkip => GetBool(R.pref_story_skip);

        public bool WithdrawEnabled => GetBool(R.pref_withdraw_enabled);

        public bool StopAfterBond10 => GetBool(R.pref_stop_bond10);

        public int BoostItemSelectionMode => GetInt(R.pref_boost_item);

        // TODO: Support debug mode
        public bool DebugMode => false;

        public IFgoSupportPreferences Support { get; }

        public IFgoRefillPreferences Refill { get; }
    }
}