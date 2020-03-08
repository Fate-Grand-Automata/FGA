using System.IO;

namespace CoreAutomata
{
    public interface IPlatformImpl
    {
        Size WindowSize { get; }

        void Scroll(Location Start, Location End);

        void Toast(string Msg);

        void Click(Location Location);

        void ContinueClick(Location Location, int Times, int Timeout = -1);

        IPattern Screenshot(Region Region, Size? TargetSize = null);

        IPattern LoadPattern(Stream Stream, Size? TargetSize = null);
    }
}