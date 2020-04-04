namespace FateGrandAutomata
{
    public static class Preferences
    {
        public static void SetPreference(IFgoPreferences Preferences)
        {
            Instance = Preferences;
        }

        public static IFgoPreferences Instance { get; private set; }
    }
}