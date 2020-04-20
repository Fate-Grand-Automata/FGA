namespace CoreAutomata
{
    public static class GameAreaManager
    {
        public static void Reset()
        {
            ScriptDimension = CompareDimension = null;
            _gameArea = null;
        }

        static Region _gameArea;

        public static Region GameArea
        {
            get => _gameArea ??= AutomataApi.WindowRegion;
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