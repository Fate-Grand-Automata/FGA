namespace CoreAutomata
{
    static class TransformationExtensions
    {
        static double? ScreenToImageScale()
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

        public static IPattern Transform(this IPattern Pattern)
        {
            var scale = ScreenToImageScale();

            return scale == null
                ? Pattern
                : Pattern.Resize(new Size(Pattern.Width, Pattern.Height) * scale.Value);
        }

        public static Location Transform(this Location Location)
        {
            var scale = ScriptToScreenScale();

            return scale == null
                ? Location
                : Location * scale.Value;
        }

        public static Region TransformToImage(this Region Region)
        {
            // Script -> Screen
            var scale1 = ScriptToScreenScale();

            // Screen -> Image
            var scale2 = ScreenToImageScale();

            if (scale1 == null && scale2 == null)
            {
                return Region;
            }

            var scale = (scale1 ?? 1) * (scale2 ?? 1);

            return Region * scale;
        }

        static double? ScriptToScreenScale()
        {
            if (GameAreaManager.ScriptDimension == null)
            {
                return null;
            }

            var sourceRegion = GameAreaManager.ScriptDimension;
            var targetRegion = GameAreaManager.GameArea;

            var pixels = sourceRegion.Pixels;

            if (sourceRegion.CompareByWidth)
            {
                if (targetRegion.W == pixels)
                {
                    return null;
                }

                return targetRegion.W / (double) pixels;
            }

            if (targetRegion.H == pixels)
            {
                return null;
            }

            return targetRegion.H / (double) pixels;
        }
    }
}