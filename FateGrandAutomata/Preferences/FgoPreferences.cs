using System;
using Android.Content;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    public class FgoPreferences : IFgoPreferences
    {
        readonly ISharedPreferences _preferences;

        public FgoPreferences(Context Context)
        {
            _preferences = PreferenceManager.GetDefaultSharedPreferences(Context);

            if (!_preferences.GetBoolean(PreferenceManager.KeyHasSetDefaultValues, false))
            {
                PreferenceManager.SetDefaultValues(Context, Resource.Xml.app_preferences, true);
                PreferenceManager.SetDefaultValues(Context, Resource.Xml.refill_preferences, true);
            }

            Refill = new FgoRefillPreferences(this);
            Support = new FgoSupportPreferences(this);
        }

        public T GetEnum<T>(string Key, T Default = default) where T: struct
        {
            return Enum.Parse<T>(_preferences.GetString(Key, Enum.GetName(typeof(T), Default)));
        }

        public bool GetBool(string Key, bool Default = false) => _preferences.GetBoolean(Key, Default);

        public string GetString(string Key, string Default = "") => _preferences.GetString(Key, Default);

        public int GetInt(string Key, int Default = 0)
        {
            var s = _preferences.GetString(Key, "");

            return int.TryParse(s, out var value) ? value : Default;
        }

        // ----- //

        public GameServer GameServer => GetEnum<GameServer>("gameserver");

        public bool SkillConfirmation => GetBool("skill_conf");

        public bool EnableAutoSkill => GetBool("autoskill_enable");

        public string SkillCommand => GetString("skill_cmd");

        public string BattleCardPriority => GetString("card_priority", "BAQ");

        public BattleNoblePhantasmType BattleNoblePhantasm => GetEnum<BattleNoblePhantasmType>("battle_np");

        public bool BattleAutoChooseTarget => GetBool("auto_choose_target");

        public bool UnstableFastSkipDeadAnimation => GetBool("fast_skip_dead");

        public bool StorySkip => GetBool("story_skip");

        public bool StopAfterBond10 => GetBool("stop_bond10");

        public int BoostItemSelectionMode => GetInt("boost_item");

        public IFgoSupportPreferences Support { get; }

        public IFgoRefillPreferences Refill { get; }
    }
}