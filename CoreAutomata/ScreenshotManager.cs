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
        static IPattern _resizeTarget;

        static IPattern GetScaledScreenshot()
        {
            var sshot = _platformImpl.Screenshot()
                .Crop(GameAreaManager.GameArea);

            var scale = TransformationExtensions.ScreenToImageScale();

            if (scale != null)
            {
                if (_resizeTarget == null)
                {
                    _resizeTarget = AutomataApi.GetResizableBlankPattern();
                }

                sshot.Resize(_resizeTarget, new Size(sshot.Width, sshot.Height) * scale.Value);

                return _resizeTarget;
            }

            return sshot;
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