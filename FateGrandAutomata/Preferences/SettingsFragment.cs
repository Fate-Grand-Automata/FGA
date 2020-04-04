using Android.OS;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    public class SettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle savedInstanceState, string rootKey)
        {
            SetPreferencesFromResource(Resource.Xml.app_preferences, rootKey);
        }
    }
}