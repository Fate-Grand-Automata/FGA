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
        static IGestureService _gesture;

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

        public static void RegisterGestures(IGestureService GestureService)
        {
            _gesture = GestureService;
        }

        public static double MinSimilarity { get; set; } = 0.8;

        public static TimeSpan DefaultHighlightTimeout { get; set; } = TimeSpan.FromSeconds(0.3);

        public static IPattern LoadPattern(Stream Stream, bool Mask = false)
        {
            return _platformImpl.LoadPattern(Stream, Mask);
        }

        public static void Wait(this TimeSpan TimeSpan)
        {
            Thread.Sleep(TimeSpan);
        }

        public static Region WindowRegion => _platformImpl.WindowRegion;

        public static void Click(this Location Location) => _gesture.Click(Location.Transform());

        static bool ExistsNow(Region Region, IPattern Image, double? Similarity)
        {
            var sshot = ScreenshotManager.GetScreenshot();

            if (Region != null)
            {
                if (_platformImpl.DebugMode)
                {
                    Region.Highlight(DefaultHighlightTimeout);
                }

                sshot = sshot.Crop(Region.TransformToImage());
            }

            return sshot
                .IsMatch(Image, Similarity ?? MinSimilarity);
        }

        static readonly Stopwatch Stopwatch = new Stopwatch();

        static bool CheckConditionLoop(Func<bool> Condition, TimeSpan? Timeout)
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

                    if (Timeout == null || Stopwatch.Elapsed > Timeout)
                    {
                        break;
                    }

                    var scanInterval = TimeSpan.FromMilliseconds(1000 / ScanRate);
                    var elapsed = Stopwatch.Elapsed - scanStartTimestamp;
                    var timeToWait = scanInterval - elapsed;

                    if (timeToWait > TimeSpan.Zero)
                    {
                        Wait(timeToWait);
                    }
                }
            }
            finally
            {
                Stopwatch.Reset();
            }

            return false;
        }

        public static bool WaitVanish(this Region Region, IPattern Image, TimeSpan? Timeout = null, double? Similarity = null)
        {
            return CheckConditionLoop(() => !ExistsNow(Region, Image, Similarity), Timeout);
        }

        public static bool Exists(this Region Region, IPattern Image, TimeSpan? Timeout = null, double? Similarity = null)
        {
            return CheckConditionLoop(() => ExistsNow(Region, Image, Similarity), Timeout);
        }

        public static double ScanRate { get; set; } = 3;

        public static IPattern GetPattern(this Region Region) => ScreenshotManager.GetScreenshot()
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

        public static void Swipe(Location Start, Location End) => _gesture.Swipe(Start.Transform(), End.Transform());

        public static IEnumerable<Match> FindAll(this Region Region, IPattern Pattern, double? Similarity = null)
        {
            var sshot = ScreenshotManager.GetScreenshot();

            if (Region != null)
            {
                if (_platformImpl.DebugMode)
                {
                    Region.Highlight(DefaultHighlightTimeout);
                }

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

        public static void ContinueClick(this Location Location, int Times)
        {
            _gesture.ContinueClick(Location.Transform(), Times);
        }

        public static void ShowMessageBox(string Title, string Message)
        {
            _platformImpl.MessageBox(Title, Message);
        }

        public static void Highlight(this Region Region, TimeSpan Timeout)
        {
            _platformImpl.Highlight(Region.Transform(), Timeout);
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