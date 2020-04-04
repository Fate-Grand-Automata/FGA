using Android.OS;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    public class SettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.app_preferences, RootKey);
        }
    }
}