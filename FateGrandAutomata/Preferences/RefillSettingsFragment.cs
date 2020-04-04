using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata.RefillSettingsFragment")]
    public class RefillSettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle savedInstanceState, string rootKey)
        {
            SetPreferencesFromResource(Resource.Xml.refill_preferences, rootKey);

            if (FindPreference("refill_repetitions") is EditTextPreference repetitionPref)
            {
                repetitionPref.MakeNumeric();
            }
        }
    }
}