namespace FateGrandAutomata
{
    public static class GameAreaManager
    {
        /// <summary>
        /// Set if we need to account for navbar above app.
        /// </summary>
        public static bool ImmersiveMode { get; set; }

        /// <summary>
        /// Automatically remove notch area from dimensions.
        /// </summary>
        public static bool AutoGameArea { get; set; }

        static Region _gameArea;

        public static Region GameArea
        {
            get
            {
                if (_gameArea != null)
                {
                    return _gameArea;
                }

                var window = Game.Impl.WindowSize;
                var region = new Region(0, 0, window.Width, window.Height);

                if (!ImmersiveMode)
                {
                    // TODO: Remove Navigation buttons. Not required for FGO.
                }

                if (AutoGameArea)
                {
                    // TODO: Remove notch area on Android P and above.
                }

                return _gameArea = region;
            }
            set => _gameArea = value;
        }

        /// <summary>
        /// Dimensions at which script was written. Compare dimension can be smaller for faster image comparison.
        /// </summary>
        public static (bool CompareByWidth, int Pixels) ScriptDimension { get; set; } = (true, -1);

        /// <summary>
        /// Image Matching dimension. All images, regions, locations are scaled to this to keep the script device independent.
        /// </summary>
        public static (bool CompareByWidth, int Pixels) CompareDimension { get; set; } = (true, -1);
    }
}