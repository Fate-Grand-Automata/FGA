using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata.SupportSettingsFragment")]
    public class SupportSettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle savedInstanceState, string rootKey)
        {
            SetPreferencesFromResource(Resource.Xml.support_preferences, rootKey);
        }
    }
}