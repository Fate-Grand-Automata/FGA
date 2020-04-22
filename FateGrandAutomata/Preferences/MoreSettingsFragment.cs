using Android.Content;
using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata." + nameof(MoreSettingsFragment))]
    public class MoreSettingsFragment : PreferenceFragmentCompat
    {
        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.app_preferences, RootKey);

            if (FindPreference(GetString(Resource.String.pref_card_priority)) is { } pref)
            {
                pref.PreferenceClick += (S, E) => StartActivity(new Intent(Activity, typeof(CardPriorityActivity)));
            }
        }

        public override void OnResume()
        {
            base.OnResume();

            if (FindPreference(GetString(Resource.String.pref_card_priority)) is { } pref)
            {
                var preferences = PreferenceManager.GetDefaultSharedPreferences(Activity);
                var key = GetString(Resource.String.pref_card_priority);
                pref.Summary = preferences.GetString(key, FgoPreferences.DefaultCardPriority);
            }
        }
    }
}