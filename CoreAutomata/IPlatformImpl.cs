using System;
using System.IO;

namespace CoreAutomata
{
    public interface IPlatformImpl
    {
        Region WindowRegion { get; }

        bool DebugMode { get; }

        void Toast(string Msg);

        IPattern LoadPattern(Stream Stream, bool Mask);

        IPattern GetResizableBlankPattern();

        void MessageBox(string Title, string Message);

        void Highlight(Region Region, TimeSpan Timeout);
    }
}