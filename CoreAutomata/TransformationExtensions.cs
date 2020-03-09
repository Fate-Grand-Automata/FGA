namespace CoreAutomata
{
    static class TransformationExtensions
    {
        public static Location Transform(this Location Location)
        {
            return new Region(Location.X, Location.Y, 0, 0)
                .Transform()
                .Location;
        }

        public static Region Transform(this Region Region)
        {
            var gameArea = GameAreaManager.GameArea;

            var (compareByWidth, pixels) = GameAreaManager.ScriptDimension;

            if (pixels == -1)
            {
                return Region;
            }

            var scale = compareByWidth
                ? gameArea.W / (double) pixels
                : gameArea.H / (double) pixels;

            return new Region((int)(Region.X * scale), (int)(Region.Y * scale), (int)(Region.W * scale), (int)(Region.H * scale));
        }
    }
}