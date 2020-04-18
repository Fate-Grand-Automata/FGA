using System.Threading.Tasks;
using Android.OS;
using Android.Runtime;
using Android.Widget;
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

            AutoSkillSettingsFragment.SetupServantAndCEPrefs(this);

            MakeNumeric(Resource.String.pref_support_swipes_per_update);
            MakeNumeric(Resource.String.pref_support_max_updates);

            if (FindPreference(GetString(Resource.String.pref_extract_def_support_imgs)) is { } pref)
            {
                pref.PreferenceClick += (S, E) =>
                {
                    Toast.MakeText(Activity, "Extracting Images in background", ToastLength.Short).Show();

                    Task.Run(ImageLocator.ExtractSupportImgs)
                        .ContinueWith(M =>
                        {
                            Activity.RunOnUiThread(() => Toast.MakeText(Activity, "Support Images Extracted Successfully", ToastLength.Short).Show());
                        });
                };
            }
        }
    }
}