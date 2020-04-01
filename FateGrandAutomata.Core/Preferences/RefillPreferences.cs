using CoreAutomata;

namespace FateGrandAutomata
{
    public class RefillPreferences : PropertyStore
    {
        public bool Enabled { get; set; }

        public int Repetitions { get; set; }

        public RefillResource Resource { get; set; }
    }
}