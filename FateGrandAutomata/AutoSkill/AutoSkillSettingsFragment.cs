using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata." + nameof(AutoSkillSettingsFragment))]
    public class AutoSkillSettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            var autoskillItemKey = Arguments.GetString(AutoSkillItemActivity.AutoSkillItemKey);

            PreferenceManager.SharedPreferencesName = autoskillItemKey;

            SetPreferencesFromResource(Resource.Xml.autoskill_item_preferences, RootKey);
        }
    }
}