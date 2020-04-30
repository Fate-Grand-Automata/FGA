using System.Collections.Generic;
using System.Linq;
using Android.Content;
using Android.OS;
using Android.Runtime;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata." + nameof(ManageAutoSkillSettingsFragment))]
    public class ManageAutoSkillSettingsFragment : PreferenceFragmentCompat
    {
        // Handle back button
        public override void OnResume()
        {
            base.OnResume();

            Init();
        }

        void Init()
        {
            if (FindPreference(GetString(Resource.String.pref_autoskill_selected)) is ListPreference list)
            {
                var key = GetString(Resource.String.pref_autoskill_list);

                var prefManager = PreferenceManager.GetDefaultSharedPreferences(Activity);
                var autoSkillItems = prefManager.GetStringSet(key, new List<string>())
                    .Select(M =>
                    {
                        var sharedPrefs = Activity.GetSharedPreferences(M, FileCreationMode.Private);

                        return (Id: M, Name: sharedPrefs.GetString(GetString(Resource.String.pref_autoskill_name), "--"));
                    })
                    .OrderBy(M => M.Name)
                    .ToArray();

                var entryValues = autoSkillItems
                    .Select(M => M.Id)
                    .ToArray();

                var entryLabels = autoSkillItems
                    .Select(M => M.Name)
                    .ToArray();

                list.SetEntryValues(entryValues);
                list.SetEntries(entryLabels);

                key = GetString(Resource.String.pref_autoskill_selected);
                var actual = prefManager.GetString(key, "");
                list.Value = ""; // Force update
                list.Value = actual;
            }
        }

        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            SetPreferencesFromResource(Resource.Xml.autoskill_preferences, RootKey);

            if (FindPreference(GetString(Resource.String.pref_autoskill_manage)) is { } manageAutoskill)
            {
                manageAutoskill.PreferenceClick += (S, E) => StartActivity(new Intent(Activity, typeof(AutoSkillActivity)));
            }
        }
    }
}