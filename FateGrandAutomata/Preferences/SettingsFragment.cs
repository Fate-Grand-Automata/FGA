using System.Collections.Generic;
using System.Linq;
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

            if (FindPreference(GetString(Resource.String.pref_autoskill_selected)) is ListPreference list)
            {
                var key = GetString(Resource.String.pref_autoskill_list);

                var prefManager = PreferenceManager.GetDefaultSharedPreferences(Activity);
                var autoSkillItems = prefManager.GetStringSet(key, new List<string>());

                var entryValues = autoSkillItems.ToArray();
                var entryLabels = autoSkillItems
                    .Select(M =>
                    {
                        var sharedPrefs = Activity.GetSharedPreferences(M, FileCreationMode.Private);

                        return sharedPrefs.GetString(GetString(Resource.String.pref_autoskill_name), "--");
                    })
                    .ToArray();

                list.SetEntryValues(entryValues);
                list.SetEntries(entryLabels);
            }
        }
    }
}