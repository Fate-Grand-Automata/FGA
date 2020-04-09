namespace CoreAutomata
{
    public static class GameAreaManager
    {
        public static void Reset()
        {
            ImmersiveMode = AutoGameArea = false;
            ScriptDimension = CompareDimension = null;
            _gameArea = null;
        }

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

                return _gameArea = AutomataApi.WindowRegion;
            }
            set => _gameArea = value;
        }

        /// <summary>
        /// Dimensions at which script was written. Compare dimension can be smaller for faster image comparison.
        /// </summary>
        public static CompareSettings ScriptDimension { get; set; }

        /// <summary>
        /// Image Matching dimension. All images, regions, locations are scaled to this to keep the script device independent.
        /// </summary>
        public static CompareSettings CompareDimension { get; set; }
    }
}