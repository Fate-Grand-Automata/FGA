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
            return _platformImpl
                .Screenshot()
                .Crop(GameAreaManager.GameArea)
                .Transform();
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