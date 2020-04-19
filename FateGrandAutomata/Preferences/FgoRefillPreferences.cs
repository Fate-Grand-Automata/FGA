using R = FateGrandAutomata.Resource.String;

namespace FateGrandAutomata
{
    public class FgoRefillPreferences : IFgoRefillPreferences
    {
        readonly FgoPreferences _preferences;
        
        public FgoRefillPreferences(FgoPreferences Preferences)
        {
            _preferences = Preferences;
        }

        public bool Enabled => _preferences.GetBool(R.pref_refill_enabled);

        public int Repetitions => _preferences.GetStringAsInt(R.pref_refill_repetitions);

        public RefillResource Resource => _preferences.GetEnum<RefillResource>(R.pref_refill_resource);
    }
}