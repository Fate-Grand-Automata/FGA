using System;
using System.Collections.Generic;

namespace FateGrandAutomata
{
    public interface IPlatformImpl
    {
        // Use same screenshot for all exists functions.
        // Not sure if I interpret this right
        void UseSameSnapIn(Action Action);

        T UseSameSnapIn<T>(Func<T> Action);

        void Scroll(Point Start, Point End);

        IEnumerable<Region> FindAll(Pattern Pattern);

        IEnumerable<Region> FindAll(Region Region, Pattern Pattern);

        void SetImmersiveMode(bool Active);

        void AutoGameArea(bool Active);

        Region GetGameArea();
        
        void SetGameArea(Region Region);

        void SetScriptDimension(bool CompareByWidth, int Pixels);
        
        void SetCompareDimension(bool CompareByWidth, int Pixels);

        void Toast(string Msg);
    }
}