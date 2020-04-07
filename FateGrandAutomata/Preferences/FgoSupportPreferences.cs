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

        public string PreferredServants
        {
            get
            {
                var prefs = _preferences.GetPreferencesForSelectedAutoSkill();

                if (prefs != null)
                {
                    var servants = prefs.GetString(_context.GetString(R.pref_autoskill_servant), "");

                    if (!string.IsNullOrWhiteSpace(servants))
                    {
                        return servants;
                    }
                }

                return _preferences.GetString(R.pref_support_pref_servant);
            }
        }

        public string PreferredCEs
        {
            get
            {
                var prefs = _preferences.GetPreferencesForSelectedAutoSkill();

                if (prefs != null)
                {
                    var ces = prefs.GetString(_context.GetString(R.pref_autoskill_ce), "");

                    if (!string.IsNullOrWhiteSpace(ces))
                    {
                        return ces;
                    }
                }

                return _preferences.GetString(R.pref_support_pref_ce);
            }
        }

        public bool FriendsOnly => _preferences.GetBool(R.pref_support_friends_only);

        public int SwipesPerUpdate => _preferences.GetInt(R.pref_support_swipes_per_update);

        public int MaxUpdates => _preferences.GetInt(R.pref_support_max_updates);

        public SupportSelectionMode SelectionMode
        {
            get
            {
                var prefs = _preferences.GetPreferencesForSelectedAutoSkill();

                if (prefs != null)
                {
                    var servants = prefs.GetString(_context.GetString(R.pref_autoskill_servant), "");
                    var ces = prefs.GetString(_context.GetString(R.pref_autoskill_ce), "");

                    if (!string.IsNullOrWhiteSpace(servants) || !string.IsNullOrWhiteSpace(ces))
                    {
                        return SupportSelectionMode.Preferred;
                    }
                }

                return _preferences.GetEnum<SupportSelectionMode>(R.pref_support_mode);
            }
        }

        public SupportSelectionMode FallbackTo => _preferences.GetEnum<SupportSelectionMode>(R.pref_support_fallback);
    }
}