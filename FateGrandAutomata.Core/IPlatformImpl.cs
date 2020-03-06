using System;
using System.Collections.Generic;

namespace FateGrandAutomata
{
    public interface IPlatformImpl
    {
        (int Width, int Height) WindowSize { get; }

        // Use same screenshot for all exists functions.
        // Not sure if I interpret this right
        void UseSameSnapIn(Action Action);

        T UseSameSnapIn<T>(Func<T> Action);

        void Scroll(Location Start, Location End);

        IEnumerable<Region> FindAll(Pattern Pattern);

        IEnumerable<Region> FindAll(Region Region, Pattern Pattern);

        void Toast(string Msg);

        void Click(Location Location);

        void ContinueClick(Location Location, int Times, int Timeout = -1);

        void WaitVanish(Region Region, Pattern Image, int? Timeout = null);

        bool Exists(Region Region, Pattern Image, int? Timeout = null, double? Similarity = null);

        Pattern Save(Region Region);
    }
}