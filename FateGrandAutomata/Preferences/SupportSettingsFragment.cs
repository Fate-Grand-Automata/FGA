using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata." + nameof(SupportSettingsFragment))]
    public class SupportSettingsFragment : PreferenceFragmentCompat
    {
        void MakeNumeric(int PreferenceKey)
        {
            if (FindPreference(GetString(PreferenceKey)) is EditTextPreference preference)
            {
                preference.MakeNumeric();
            }
        }

        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.support_preferences, RootKey);

            MakeNumeric(Resource.String.pref_support_swipes_per_update);
            MakeNumeric(Resource.String.pref_support_max_updates);
        }
    }
}