namespace FateGrandAutomata
{
    public interface IFgoRefillPreferences
    {
        bool Enabled { get; }

        int Repetitions { get; }

        RefillResource Resource { get; }
    }
}