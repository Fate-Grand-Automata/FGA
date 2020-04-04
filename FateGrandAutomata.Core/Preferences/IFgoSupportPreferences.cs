namespace FateGrandAutomata
{
    public interface IFgoSupportPreferences
    {
        string FriendNames { get; }

        string PreferredServants { get; }

        string PreferredCEs { get; }

        bool FriendsOnly { get; }

        int SwipesPerUpdate { get; }

        int MaxUpdates { get; }

        SupportSelectionMode SelectionMode { get; }

        SupportSelectionMode FallbackTo { get; }
    }
}