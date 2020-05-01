namespace CoreAutomata
{
    static class TransformationExtensions
    {
        public static double? ScreenToImageScale()
        {
            var targetDimensions = GameAreaManager.CompareDimension ?? GameAreaManager.ScriptDimension;

            if (targetDimensions == null)
            {
                return null;
            }

            var gameArea = GameAreaManager.GameArea;

            if (targetDimensions.CompareByWidth)
            {
                if (targetDimensions.Pixels == gameArea.W)
                {
                    return null;
                }

                return targetDimensions.Pixels / (double) gameArea.W;
            }

            if (targetDimensions.Pixels == gameArea.H)
            {
                return null;
            }

            return targetDimensions.Pixels / (double) gameArea.H;
        }

        public static Location Transform(this Location Location)
        {
            var scale = ScriptToScreenScale();

            var scaledPoint = Location * scale;

            var gameArea = GameAreaManager.GameArea;

            return new Location(scaledPoint.X + gameArea.X, scaledPoint.Y + gameArea.Y);
        }

        public static Region Transform(this Region Region)
        {
            var scale = ScriptToScreenScale();

            var trLoc = Region.Location.Transform();
            var size = new Size(Region.W, Region.H);
            var scaledSize = size * scale;

            return new Region(trLoc.X, trLoc.Y, scaledSize.Width, scaledSize.Height);
        }

        public static Region TransformToImage(this Region Region)
        {
            // Script -> Screen
            var scale1 = ScriptToScreenScale();

            // Screen -> Image
            var scale2 = ScreenToImageScale();

            var scale = scale1 * (scale2 ?? 1);

            return Region * scale;
        }

        public static Region TransformFromImage(this Region Region)
        {
            // Script -> Screen
            var scale1 = ScriptToScreenScale();

            // Screen -> Image
            var scale2 = ScreenToImageScale();

            var scale = scale1 * (scale2 ?? 1);

            return Region * (1 / scale);
        }

        const double DontScale = 1;

        static double ScriptToScreenScale()
        {
            if (GameAreaManager.ScriptDimension == null)
            {
                return DontScale;
            }

            var sourceRegion = GameAreaManager.ScriptDimension;
            var targetRegion = GameAreaManager.GameArea;

            var pixels = sourceRegion.Pixels;

            if (sourceRegion.CompareByWidth)
            {
                if (targetRegion.W == pixels)
                {
                    return DontScale;
                }

                return targetRegion.W / (double) pixels;
            }

            if (targetRegion.H == pixels)
            {
                return DontScale;
            }

            return targetRegion.H / (double) pixels;
        }
    }
}