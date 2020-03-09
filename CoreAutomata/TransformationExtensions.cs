namespace CoreAutomata
{
    static class TransformationExtensions
    {
        static double ScreenToImageScale()
        {
            var targetDimensions = GameAreaManager.CompareDimension ?? GameAreaManager.ScriptDimension;

            if (targetDimensions == null)
            {
                return 1;
            }

            var gameArea = GameAreaManager.GameArea;

            var scale = targetDimensions.CompareByWidth
                ? targetDimensions.Pixels / (double)gameArea.W
                : targetDimensions.Pixels / (double)gameArea.H;

            return scale;
        }

        public static IPattern Transform(this IPattern Pattern)
        {
            var scale = ScreenToImageScale();

            return Pattern.Resize(new Size(Pattern.Width, Pattern.Height) * scale);
        }

        public static Location Transform(this Location Location)
        {
            return new Region(Location.X, Location.Y, 0, 0)
                .Transform()
                .Location;
        }

        public static Location TransformToImage(this Location Location)
        {
            return new Region(Location.X, Location.Y, 0, 0)
                .TransformToImage()
                .Location;
        }

        public static Region TransformToImage(this Region Region)
        {
            // Script -> Screen
            Region = Region.Transform();

            // Screen -> Image
            var scale = ScreenToImageScale();

            return Region * scale;
        }

        public static Region Transform(this Region Region)
        {
            if (GameAreaManager.ScriptDimension == null)
            {
                return Region;
            }

            var sourceRegion = GameAreaManager.ScriptDimension;
            var targetRegion = GameAreaManager.GameArea;

            var pixels = sourceRegion.Pixels;

            var scale = sourceRegion.CompareByWidth
                ? targetRegion.W / (double)pixels
                : targetRegion.H / (double)pixels;

            return Region * scale;
        }
    }
}