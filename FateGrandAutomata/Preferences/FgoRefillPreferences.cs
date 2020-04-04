namespace FateGrandAutomata
{
    public class FgoRefillPreferences : IFgoRefillPreferences
    {
        readonly FgoPreferences _preferences;
        
        public FgoRefillPreferences(FgoPreferences Preferences)
        {
            _preferences = Preferences;
        }

        public bool Enabled => _preferences.GetBool("refill_enabled");

        public int Repetitions => _preferences.GetInt("refill_repetitions");

        public RefillResource Resource => _preferences.GetEnum<RefillResource>("refill_resource");
    }
}