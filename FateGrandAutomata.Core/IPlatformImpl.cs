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

        /// <summary>
        /// Set if we need to account for navbar above app.
        /// </summary>
        bool ImmersiveMode { get; set; }

        void AutoGameArea(bool Active);

        Region GetGameArea();
        
        void SetGameArea(Region Region);

        void SetScriptDimension(bool CompareByWidth, int Pixels);
        
        /// <summary>
        /// Image Matching dimension. All images, regions, locations
        /// </summary>
        void SetCompareDimension(bool CompareByWidth, int Pixels);

        void Toast(string Msg);

        void Click(Location Location);

        void WaitVanish(Region Region, Pattern Image, int? Timeout = null);

        bool Exists(Region Region, Pattern Image, int? Timeout = null);

        Pattern Save(Region Region);
    }
}