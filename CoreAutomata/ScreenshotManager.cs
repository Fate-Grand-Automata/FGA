namespace CoreAutomata
{
    public static class ScreenshotManager
    {
        static IPlatformImpl _platformImpl;

        public static void RegisterPlatform(IPlatformImpl Impl)
        {
            _platformImpl = Impl;
        }

        public static bool UsePreviousSnap { get; set; }

        static IPattern _previousPattern;

        static IPattern GetScaledScreenshot()
        {
            var sshot = _platformImpl.Screenshot();
            var cutout = AutomataApi.Cutout;

            if (cutout != null)
            {
                var (l, t, r, b) = cutout.Value;
                var w = sshot.Width - l - r;
                var h = sshot.Height - t - b;
                sshot.Crop(new Region(l, t, w, h));
            }

            return sshot.Transform();
        }

        public static void Snapshot()
        {
            _previousPattern = GetScaledScreenshot();
            UsePreviousSnap = true;
        }

        public static IPattern GetScreenshot()
        {
            if (UsePreviousSnap)
            {
                return _previousPattern;
            }

            return _previousPattern = GetScaledScreenshot();
        }
    }
}