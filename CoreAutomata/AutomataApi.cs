using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading;

namespace CoreAutomata
{
    public static class AutomataApi
    {
        static IPlatformImpl _platformImpl;

        static AutomataApi()
        {
            DebugMsgReceived += Msg =>
            {
                Msg = $"DBG: {Msg}";

                Console.WriteLine(Msg);

                Toast(Msg);
            };
        }

        public static void RegisterPlatform(IPlatformImpl Impl)
        {
            _platformImpl = Impl;
        }

        public static double MinSimilarity { get; set; } = 0.8;

        public static IPattern LoadPattern(Stream Stream)
        {
            return _platformImpl.LoadPattern(Stream);
        }

        public static void Wait(double Seconds)
        {
            Thread.Sleep(TimeSpan.FromSeconds(Seconds));
        }

        public static Region WindowRegion => _platformImpl.WindowRegion;

        public static void Click(Location Location) => _platformImpl.Click(Location.Transform());

        static bool ExistsNow(Region Region, IPattern Image, double? Similarity)
        {
            var sshot = ScreenshotManager.GetScreenshot();

            if (Region != null)
            {
                sshot = sshot.Crop(Region.TransformToImage());
            }

            return sshot
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

                    var scanInterval = TimeSpan.FromMilliseconds(1000 / ScanRate);
                    var elapsed = Stopwatch.Elapsed - scanStartTimestamp;
                    var timeToWaitSec = (scanInterval - elapsed).TotalSeconds;

                    if (timeToWaitSec > 0)
                    {
                        Wait(timeToWaitSec);
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

        public static IPattern GetCopy(Region Region) => ScreenshotManager.GetScreenshot()
            .Crop(Region.TransformToImage())
            .Copy();

        public static void UseSameSnapIn(Action Action) => UseSameSnapIn(() =>
        {
            Action();
            return 0;
        });

        public static T UseSameSnapIn<T>(Func<T> Action)
        {
            ScreenshotManager.Snapshot();

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

        public static IEnumerable<Match> FindAll(Region Region, IPattern Pattern, double? Similarity = null)
        {
            var sshot = ScreenshotManager.GetScreenshot();

            if (Region != null)
            {
                sshot = sshot.Crop(Region.TransformToImage());
            }

            return sshot
                .FindMatches(Pattern, Similarity ?? MinSimilarity)
                .Select(M =>
                {
                    var region = M.TransformFromImage();

                    if (Region != null)
                    {
                        region.X += Region.X;
                        region.Y += Region.Y;
                    }
                    
                    return new Match(region, M.Score);
                });
        }

        public static IPattern GetResizableBlankPattern() => _platformImpl.GetResizableBlankPattern();

        public static void Toast(string Msg) => _platformImpl?.Toast(Msg);

        public static void ContinueClick(Location Location, int Times)
        {
            _platformImpl.ContinueClick(Location.Transform(), Times);
        }

        public static void ShowMessageBox(string Title, string Message)
        {
            _platformImpl.MessageBox(Title, Message);
        }

        public static void SetStorageRootDir(string Dir)
        {
            _storageRootDir = Dir;
        }

        static string _storageRootDir;

        public static string StorageDir
        {
            get
            {
                var dir = Path.Combine(_storageRootDir, "Fate-Grand-Automata");

                if (!Directory.Exists(dir))
                {
                    Directory.CreateDirectory(dir);
                }

                return dir;
            }
        }

        [Conditional("DEBUG")]
        public static void WriteDebug(string Msg)
        {
            DebugMsgReceived?.Invoke(Msg);
        }

        public static event Action<string> DebugMsgReceived;
    }
}