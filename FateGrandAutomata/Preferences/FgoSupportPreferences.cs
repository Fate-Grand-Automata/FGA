using R = FateGrandAutomata.Resource.String;

namespace FateGrandAutomata
{
    public class FgoSupportPreferences : IFgoSupportPreferences
    {
        readonly FgoPreferences _preferences;

        public FgoSupportPreferences(FgoPreferences Preferences)
        {
            _preferences = Preferences;
        }

        public string FriendNames => _preferences.GetString(R.pref_support_friend_names);

        public string PreferredServants => _preferences.GetString(R.pref_support_pref_servant);

        public string PreferredCEs => _preferences.GetString(R.pref_support_pref_ce);

        public bool FriendsOnly => _preferences.GetBool(R.pref_support_friends_only);

        public int SwipesPerUpdate => _preferences.GetInt(R.pref_support_swipes_per_update);

        public int MaxUpdates => _preferences.GetInt(R.pref_support_max_updates);

        public SupportSelectionMode SelectionMode => _preferences.GetEnum<SupportSelectionMode>(R.pref_support_mode);

        public SupportSelectionMode FallbackTo => _preferences.GetEnum<SupportSelectionMode>(R.pref_support_fallback);
    }
}