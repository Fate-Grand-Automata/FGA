namespace CoreAutomata
{
    public static class ScreenshotManager
    {
        static IScreenshotService _impl;

        public static void Register(IScreenshotService Impl)
        {
            _impl = Impl;
        }

        public static bool UsePreviousSnap { get; set; }

        static IPattern _previousPattern;
        static IPattern _resizeTarget;

        static IPattern GetScaledScreenshot()
        {
            var sshot = _impl.TakeScreenshot()
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

        public static void ReleaseMemory()
        {
            _previousPattern?.Dispose();
            _previousPattern = null;

            _resizeTarget?.Dispose();
            _resizeTarget = null;
        }
    }
}