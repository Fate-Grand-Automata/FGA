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
        public override void OnResume()
        {
            base.OnResume();

            AutoSkillSettingsFragment.PreferredSupportOnResume(this);
        }

        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.support_preferences, RootKey);

            AutoSkillSettingsFragment.PreferredSupportOnCreate(this);

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