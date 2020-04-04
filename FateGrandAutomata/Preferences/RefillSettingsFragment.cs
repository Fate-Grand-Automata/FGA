using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata.RefillSettingsFragment")]
    public class RefillSettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.refill_preferences, RootKey);

            if (FindPreference(GetString(Resource.String.pref_refill_repetitions)) is EditTextPreference repetitionPref)
            {
                repetitionPref.MakeNumeric();
            }
        }
    }
}