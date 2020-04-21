﻿using System.Collections.Generic;
using System.IO;
using System.Linq;
using Android.Content;
using R = FateGrandAutomata.Resource.String;

namespace FateGrandAutomata
{
    public class FgoSupportPreferences : IFgoSupportPreferences
    {
        readonly FgoPreferences _preferences;
        readonly Context _context;

        public FgoSupportPreferences(FgoPreferences Preferences, Context Context)
        {
            _preferences = Preferences;
            _context = Context;
        }

        public string FriendNames => _preferences.GetString(R.pref_support_friend_names);

        int GetAutoskillPreferredServantCount()
        {
            var prefs = _preferences.GetPreferencesForSelectedAutoSkill();

            if (prefs == null)
            {
                return 0;
            }

            var servants = prefs.GetStringSet(_context.GetString(R.pref_support_pref_servant), new List<string>());

            return servants.Count;
        }

        public string PreferredServants
        {
            get
            {
                var prefs = GetAutoskillPreferredServantCount() > 0
                    ? _preferences.GetPreferencesForSelectedAutoSkill()
                    : _preferences.DefaultPrefs;

                var servantSet =
                    prefs.GetStringSet(_context.GetString(R.pref_support_pref_servant), new List<string>());

                var servants = new List<string>();

                foreach (var servEntry in servantSet)
                {
                    var path = Path.Combine(ImageLocator.SupportServantImgFolder, servEntry);

                    if (Directory.Exists(path))
                    {
                        var fileNames = Directory
                            .EnumerateFiles(path)
                            .Select(M => Path.Combine(servEntry, Path.GetFileName(M)));

                        servants.AddRange(fileNames);
                    }
                    else servants.Add(servEntry);
                }

                var servantImgFolderName = Path.GetFileName(ImageLocator.SupportServantImgFolder);

                if (servants.Count > 0)
                {
                    return string.Join(", ", servants.Select(M => Path.Combine(servantImgFolderName, M)));
                }

                return "";
            }
        }

        int GetAutoskillPreferredCEsCount()
        {
            var prefs = _preferences.GetPreferencesForSelectedAutoSkill();

            if (prefs == null)
            {
                return 0;
            }

            var ces = prefs.GetStringSet(_context.GetString(R.pref_support_pref_ce), new List<string>());

            return ces.Count;
        }

        public string PreferredCEs
        {
            get
            {
                var prefs = GetAutoskillPreferredCEsCount() > 0
                    ? _preferences.GetPreferencesForSelectedAutoSkill()
                    : _preferences.DefaultPrefs;

                var ceSet = prefs.GetStringSet(_context.GetString(R.pref_support_pref_ce), new List<string>());

                var ceImgFolderName = Path.GetFileName(ImageLocator.SupportCeImgFolder);

                var ces = ceSet.Select(M => Path.Combine(ceImgFolderName, M)).ToList();

                if (ces.Count > 0)
                {
                    var isMlb = prefs.GetBoolean(_context.GetString(R.pref_support_pref_ce_mlb), false);

                    return string.Join(", ", ces
                        .Select(M => isMlb ? $"{Support.LimitBrokenCharacter}{M}" : M));
                }

                return "";
            }
        }

        public bool FriendsOnly => _preferences.GetBool(R.pref_support_friends_only);

        public int SwipesPerUpdate => _preferences.GetInt(R.pref_support_swipes_per_update);

        public int MaxUpdates => _preferences.GetInt(R.pref_support_max_updates);

        public SupportSelectionMode SelectionMode
        {
            get
            {
                var servants = GetAutoskillPreferredServantCount();
                var ces = GetAutoskillPreferredCEsCount();

                if (servants > 0 || ces > 0)
                {
                    return SupportSelectionMode.Preferred;
                }

                return _preferences.GetEnum<SupportSelectionMode>(R.pref_support_mode);
            }
        }

        public SupportSelectionMode FallbackTo => _preferences.GetEnum<SupportSelectionMode>(R.pref_support_fallback);
    }
}