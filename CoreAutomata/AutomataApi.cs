using System;
using System.Collections.Generic;
using System.Threading;

namespace CoreAutomata
{
    public static class AutomataApi
    {
        static IPlatformImpl _platformImpl;

        public static void RegisterPlatform(IPlatformImpl Impl)
        {
            _platformImpl = Impl;
        }

        public static void Wait(double Seconds)
        {
            Thread.Sleep(TimeSpan.FromSeconds(Seconds));
        }

        public static (int Width, int Height) WindowSize => _platformImpl.WindowSize;

        public static void Click(Location Location) => _platformImpl.Click(Location);

        public static void WaitVanish(Region Region, Pattern Image, int? Timeout = null)
        {
            throw new NotImplementedException();
        }

        public static bool Exists(Region Region, Pattern Image, int? Timeout = null, double? Similarity = null)
        {
            throw new NotImplementedException();
        }

        public static Pattern Save(Region Region) => _platformImpl.Screenshot(Region);

        public static void UseSameSnapIn(Action Action) => throw new NotImplementedException();

        public static T UseSameSnapIn<T>(Func<T> Action) => throw new NotImplementedException();

        public static void Scroll(Location Start, Location End) => _platformImpl.Scroll(Start, End);

        public static IEnumerable<Region> FindAll(Pattern Pattern)
        {
            throw new NotImplementedException();
        }

        public static IEnumerable<Region> FindAll(Region Region, Pattern Pattern)
        {
            throw new NotImplementedException();
        }

        public static void Toast(string Msg) => _platformImpl.Toast(Msg);

        public static void ContinueClick(Location Location, int Times, int Timeout = -1)
        {
            _platformImpl.ContinueClick(Location, Times, Timeout);
        }
    }
}