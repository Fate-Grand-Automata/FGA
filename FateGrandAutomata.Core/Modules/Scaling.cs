using System;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class Scaling
    {
        (bool ByWidth, double Rate) DecideScaleMethod(int OriginalWidth, int OriginalHeight, int DesiredWidth, int DesiredHeight)
        {
            var rateToScaleByWidth = DesiredWidth / (double) OriginalWidth;
            var rateToScaleByHeight = DesiredHeight / (double) OriginalHeight;

            return rateToScaleByWidth <= rateToScaleByHeight
                ? (true, rateToScaleByWidth)
                : (false, rateToScaleByHeight);
        }

        (int Width, int Height) Scale(int OriginalWidth, int OriginalHeight, double Rate)
        {
            var w = (int) Math.Round(OriginalWidth * Rate);
            var h = (int) Math.Round(OriginalHeight * Rate);

            return (w, h);
        }

        int CalculateBorderThickness(int Outer, int Inner)
        {
            var size = Math.Abs(Outer - Inner);

            return (int) Math.Round(size / 2.0);
        }

        Region CalculateGameAreaWithoutBorders(int ScriptWidth,
            int ScriptHeight,
            int ScreenWidth,
            int ScreenHeight,
            double ScaleRate)
        {
            var (scaledW, scaledH) = Scale(ScriptWidth, ScriptHeight, ScaleRate);

            return new Region(
                CalculateBorderThickness(ScreenWidth, scaledW), // Offset (X)
                CalculateBorderThickness(ScreenHeight, scaledH), // Offset (Y)
                scaledW, // Game Width (without borders)
                scaledH // Game Height (without borders)
            );
        }

        Region ApplyNotchOffset(Region Region, int NotchOffset)
        {
            Region.X += NotchOffset;

            return Region;
        }

        public void ApplyAspectRatioFix(int ScriptWidth, int ScriptHeight, int ImageWidth, int ImageHeight)
        {
            GameAreaManager.ImmersiveMode = true;
            GameAreaManager.AutoGameArea = true;

            var gameWithBorders = GameAreaManager.GameArea;
            var (scaleByWidth, scaleRate) = DecideScaleMethod(ScriptWidth, ScriptHeight,
                gameWithBorders.W, gameWithBorders.H);
            var gameWithoutBorders = CalculateGameAreaWithoutBorders(ScriptWidth, ScriptHeight,
                gameWithBorders.W, gameWithBorders.H,
                scaleRate);
            var gameWithoutBordersAndNotch = ApplyNotchOffset(gameWithoutBorders, gameWithBorders.X);

            GameAreaManager.GameArea = gameWithoutBordersAndNotch;

            if (scaleByWidth)
            {
                GameAreaManager.ScriptDimension = new CompareSettings(true, ScriptWidth);
                GameAreaManager.CompareDimension = new CompareSettings(true, ImageWidth);
            }
            else
            {
                GameAreaManager.ScriptDimension = new CompareSettings(false, ScriptHeight);
                GameAreaManager.CompareDimension = new CompareSettings(false, ImageHeight);
            }
        }

        static bool _initialized;

        public static void Init()
        {
            if (_initialized)
            {
                return;
            }

            var scaling = new Scaling();
            
            // Set only ONCE
            scaling.ApplyAspectRatioFix(Game.ScriptWidth,
                Game.ScriptHeight,
                Game.ImageWidth,
                Game.ImageHeight);

            _initialized = true;
        }
    }
}