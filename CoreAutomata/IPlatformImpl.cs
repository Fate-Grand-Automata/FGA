namespace CoreAutomata
{
    public interface IPlatformImpl
    {
        (int Width, int Height) WindowSize { get; }

        void Scroll(Location Start, Location End);

        void Toast(string Msg);

        void Click(Location Location);

        void ContinueClick(Location Location, int Times, int Timeout = -1);

        Pattern Screenshot(Region Region);
    }
}