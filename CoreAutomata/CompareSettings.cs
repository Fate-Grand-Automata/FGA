namespace CoreAutomata
{
    public class CompareSettings
    {
        public bool CompareByWidth { get; }

        public int Pixels { get; }

        public CompareSettings(bool CompareByWidth, int Pixels)
        {
            this.CompareByWidth = CompareByWidth;
            this.Pixels = Pixels;
        }
    }
}