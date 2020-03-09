using System;
using System.Collections.Generic;
using System.IO;
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

        public static IPattern LoadPattern(Stream Stream)
        {
            return _platformImpl.LoadPattern(Stream);
        }

        public static void Wait(double Seconds)
        {
            Thread.Sleep(TimeSpan.FromSeconds(Seconds));
        }

        public static Size WindowSize => _platformImpl.WindowSize;

        public static void Click(Location Location) => _platformImpl.Click(Location.Transform());

        public static void WaitVanish(Region Region, IPattern Image, int? Timeout = null)
        {
            throw new NotImplementedException();
        }

        public static bool Exists(Region Region, IPattern Image, int? Timeout = null, double? Similarity = null)
        {
            throw new NotImplementedException();
        }

        public static IPattern Save(Region Region) => _platformImpl.Screenshot().Crop(Region.Transform());

        public static void UseSameSnapIn(Action Action) => throw new NotImplementedException();

        public static T UseSameSnapIn<T>(Func<T> Action) => throw new NotImplementedException();

        public static void Scroll(Location Start, Location End) => _platformImpl.Scroll(Start.Transform(), End.Transform());

        public static IEnumerable<Region> FindAll(IPattern Pattern)
        {
            throw new NotImplementedException();
        }

        public static IEnumerable<Region> FindAll(Region Region, IPattern Pattern)
        {
            throw new NotImplementedException();
        }

        public static void Toast(string Msg) => _platformImpl.Toast(Msg);

        public static void ContinueClick(Location Location, int Times, int Timeout = -1)
        {
            _platformImpl.ContinueClick(Location.Transform(), Times, Timeout);
        }
    }
}