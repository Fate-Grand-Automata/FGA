using Android.OS;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    public class MainSettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.main_preferences, RootKey);
        }
    }
}