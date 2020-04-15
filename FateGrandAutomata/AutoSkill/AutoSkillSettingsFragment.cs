using System.Collections.Generic;
using System.IO;
using System.Linq;
using Android.Content;
using Android.OS;
using Android.Runtime;
using AndroidX.AppCompat.App;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    [Register("fgautomata." + nameof(AutoSkillSettingsFragment))]
    public class AutoSkillSettingsFragment : PreferenceFragmentCompat
    {
        public static void SetupServantAndCEPrefs(PreferenceFragmentCompat Pref)
        {
            if (Pref.FindPreference(Pref.GetString(Resource.String.pref_support_pref_servant)) is MultiSelectListPreference pref)
            {
                var entries = Directory
                    .EnumerateFileSystemEntries(ImageLocator.SupportServantImgFolder)
                    .Select(Path.GetFileName)
                    .OrderBy(M => M)
                    .ToArray();

                pref.SetEntryValues(entries);
                pref.SetEntries(entries);
                pref.SummaryProvider = new MultiSelectListSummaryProvider();

                if (Pref.FindPreference(Pref.GetString(Resource.String.pref_support_pref_servant_clear)) is { } servClear)
                {
                    servClear.PreferenceClick += (S, E) => pref.Values = new List<string>();
                }
            }

            if (Pref.FindPreference(Pref.GetString(Resource.String.pref_support_pref_ce)) is MultiSelectListPreference prefce)
            {
                var entries = Directory
                    .EnumerateFiles(ImageLocator.SupportCeImgFolder)
                    .Select(Path.GetFileName)
                    .Append(MonaLisa)
                    .Append(ChaldeaLunchtime)
                    .OrderBy(M => M)
                    .Distinct()
                    .ToArray();

                prefce.SetEntryValues(entries);
                prefce.SetEntries(entries);
                prefce.SummaryProvider = new MultiSelectListSummaryProvider();

                if (Pref.FindPreference(Pref.GetString(Resource.String.pref_support_pref_ce_clear)) is { } ceClear)
                {
                    ceClear.PreferenceClick += (S, E) => prefce.Values = new List<string>();
                }
            }
        }

        public override void OnCreatePreferences(Bundle SavedInstanceState, string RootKey)
        {
            var autoskillItemKey = Arguments.GetString(AutoSkillItemActivity.AutoSkillItemKey);

            PreferenceManager.SharedPreferencesName = autoskillItemKey;

            SetPreferencesFromResource(Resource.Xml.autoskill_item_preferences, RootKey);

            SetupServantAndCEPrefs(this);

            if (FindPreference(GetString(Resource.String.pref_autoskill_delete)) is { } deleteBtn)
            {
                void OnDeleteBtnOnPreferenceClick(object S, Preference.PreferenceClickEventArgs E)
                {
                    new AlertDialog.Builder(Activity)
                        .SetMessage("Are you sure you want to delete this configuration?")
                        .SetTitle("Confirm Deletion")
                        .SetPositiveButton("Delete", (S, E) =>
                        {
                            DeleteItem(autoskillItemKey);
                        })
                        .SetNegativeButton("Cancel", (S, E) => { })
                        .Show();
                }

                deleteBtn.PreferenceClick += OnDeleteBtnOnPreferenceClick;
            }
        }

        public const string MonaLisa = "mona_lisa.png";
        public const string ChaldeaLunchtime = "chaldea_lunchtime.png";

        void DeleteItem(string AutoskillItemKey)
        {
            Activity.DeleteSharedPreferences(AutoskillItemKey);

            var prefs = PreferenceManager.GetDefaultSharedPreferences(Activity);

            var autoskillItemsKeys = GetString(Resource.String.pref_autoskill_list);
            var autoskillItems = prefs.GetStringSet(autoskillItemsKeys, new List<string>())
                .ToList();
            autoskillItems.Remove(AutoskillItemKey);

            prefs
                .Edit()
                .PutStringSet(autoskillItemsKeys, autoskillItems)
                .Commit();

            UnselectItem(AutoskillItemKey, prefs, autoskillItems);

            // We opened a separate activity for autoskill item
            Activity.Finish();
        }

        void UnselectItem(string AutoskillItemKey, ISharedPreferences Prefs, IReadOnlyCollection<string> AutoskillItems)
        {
            var selectedAutoskillKey = GetString(Resource.String.pref_autoskill_selected);
            var selectedAutoSkill = Prefs.GetString(selectedAutoskillKey, "");

            if (selectedAutoSkill == AutoskillItemKey)
            {
                selectedAutoSkill = AutoskillItems.Count > 0 ? AutoskillItems.First() : "";

                Prefs.Edit()
                    .PutString(selectedAutoskillKey, selectedAutoSkill)
                    .Commit();
            }
        }
    }
}