namespace FateGrandAutomata
{
    public class FgoSupportPreferences : IFgoSupportPreferences
    {
        readonly FgoPreferences _preferences;

        public FgoSupportPreferences(FgoPreferences Preferences)
        {
            _preferences = Preferences;
        }

        public string FriendNames => _preferences.GetString("support_friend_names");

        public string PreferredServants => _preferences.GetString("support_pref_servant");

        public string PreferredCEs => _preferences.GetString("support_pref_ce");

        public bool FriendsOnly => _preferences.GetBool("support_friends_only");

        public int SwipesPerUpdate => _preferences.GetInt("support_swiper_per_update");

        public int MaxUpdates => _preferences.GetInt("support_max_updates");

        public SupportSelectionMode SelectionMode => _preferences.GetEnum<SupportSelectionMode>("support_mode");

        public SupportSelectionMode FallbackTo => _preferences.GetEnum<SupportSelectionMode>("support_fallback");
    }
}