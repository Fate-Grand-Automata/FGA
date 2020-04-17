using Android.Content;
using Android.OS;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    public class SettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.app_preferences, RootKey);

            if (FindPreference(GetString(Resource.String.pref_card_priority)) is { } pref)
            {
                pref.PreferenceClick += (S, E) => StartActivity(new Intent(Activity, typeof(CardPriorityActivity)));
            }
        }
    }
}