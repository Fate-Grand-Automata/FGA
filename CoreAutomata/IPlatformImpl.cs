﻿using System.IO;

namespace CoreAutomata
{
    public interface IPlatformImpl
    {
        Region WindowRegion { get; }

        void Scroll(Location Start, Location End);

        void Toast(string Msg);

        void Click(Location Location);

        void ContinueClick(Location Location, int Times);

        IPattern Screenshot();

        IPattern LoadPattern(Stream Stream);

        void MessageBox(string Title, string Message);
    }
}