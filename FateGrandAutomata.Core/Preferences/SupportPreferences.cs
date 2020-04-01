using CoreAutomata;

namespace FateGrandAutomata
{
    public class SupportPreferences : PropertyStore
    {
        public string FriendNames { get; set; }

        public string PreferredServants { get; set; }

        public string PreferredCEs { get; set; } = "*mona_lisa.png";

        public bool FriendsOnly { get; set; }

        public int SwipesPerUpdate { get; set; } = 10;

        public int MaxUpdates { get; set; } = 3;

        public SupportSelectionMode SelectionMode { get; set; } = SupportSelectionMode.Preferred;

        public SupportSelectionMode FallbackTo { get; set; } = SupportSelectionMode.First;
    }
}