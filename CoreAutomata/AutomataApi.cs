using System;
using System.Collections.Generic;
using System.Diagnostics;
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

            ScreenshotManager.RegisterPlatform(Impl);
        }

        public static double MinSimilarity { get; set; } = 0.85;

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

        static bool ExistsNow(Region Region, IPattern Image, double? Similarity)
        {
            return ScreenshotManager.GetScreenshot()
                .Crop(Region.TransformToImage())
                .IsMatch(Image, Similarity ?? MinSimilarity);
        }

        static readonly Stopwatch Stopwatch = new Stopwatch();

        static bool CheckConditionLoop(Func<bool> Condition, double? Timeout)
        {
            Stopwatch.Restart();

            try
            {
                while (true)
                {
                    var scanStartTimestamp = Stopwatch.Elapsed;

                    if (Condition.Invoke())
                    {
                        return true;
                    }

                    if (Timeout == null || Stopwatch.Elapsed.TotalSeconds > Timeout)
                    {
                        break;
                    }

                    var scanIntervalMs = 1000 / ScanRate;
                    var timeToWaitMs = (scanStartTimestamp + TimeSpan.FromMilliseconds(scanIntervalMs) - Stopwatch.Elapsed).TotalMilliseconds;

                    if (timeToWaitMs > 0)
                    {
                        Wait(timeToWaitMs);
                    }
                }
            }
            finally
            {
                Stopwatch.Reset();
            }

            return false;
        }

        public static bool WaitVanish(Region Region, IPattern Image, int? Timeout = null, double? Similarity = null)
        {
            return CheckConditionLoop(() => !ExistsNow(Region, Image, Similarity), Timeout);
        }

        public static bool Exists(Region Region, IPattern Image, int? Timeout = null, double? Similarity = null)
        {
            return CheckConditionLoop(() => ExistsNow(Region, Image, Similarity), Timeout);
        }

        public static double ScanRate { get; set; } = 3;

        public static IPattern Save(Region Region) => ScreenshotManager.GetScreenshot()
            .Crop(Region.TransformToImage());

        public static void UseSameSnapIn(Action Action) => UseSameSnapIn(() =>
        {
            Action();
            return 0;
        });

        public static T UseSameSnapIn<T>(Func<T> Action)
        {
            ScreenshotManager.Snapshot();
            ScreenshotManager.UsePreviousSnap = true;

            try
            {
                return Action();
            }
            finally
            {
                ScreenshotManager.UsePreviousSnap = false;
            }
        }

        public static void Scroll(Location Start, Location End) => _platformImpl.Scroll(Start.Transform(), End.Transform());

        public static IEnumerable<Match> FindAll(IPattern Pattern, double? Similarity = null)
        {
            return ScreenshotManager.GetScreenshot()
                .FindMatches(Pattern, Similarity ?? MinSimilarity);
        }

        public static IEnumerable<Match> FindAll(Region Region, IPattern Pattern, double? Similarity = null)
        {
            return ScreenshotManager.GetScreenshot()
                .Crop(Region.TransformToImage())
                .FindMatches(Pattern, Similarity ?? MinSimilarity);
        }

        public static void Toast(string Msg) => _platformImpl.Toast(Msg);

        public static void ContinueClick(Location Location, int Times, int Timeout = -1)
        {
            _platformImpl.ContinueClick(Location.Transform(), Times, Timeout);
        }
    }
}