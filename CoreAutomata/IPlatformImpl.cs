using System.IO;

namespace CoreAutomata
{
    public interface IPlatformImpl
    {
        Region WindowRegion { get; }

        void Toast(string Msg);

        IPattern LoadPattern(Stream Stream);

        IPattern GetResizableBlankPattern();

        void MessageBox(string Title, string Message);
    }
}