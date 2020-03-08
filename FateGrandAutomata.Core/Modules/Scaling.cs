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
            var scriptScaledToScreen = Scale(ScriptWidth, ScriptHeight, ScaleRate);

            return new Region(
                CalculateBorderThickness(ScreenWidth, scriptScaledToScreen.Width), // Offset (X)
                CalculateBorderThickness(ScreenHeight, scriptScaledToScreen.Height), // Offset (Y)
                scriptScaledToScreen.Width, // Game Width (without borders)
                scriptScaledToScreen.Height // Game Height (without borders)
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
            var scaleMethod = DecideScaleMethod(ScriptWidth, ScriptHeight,
                gameWithBorders.W, gameWithBorders.H);
            var gameWithoutBorders = CalculateGameAreaWithoutBorders(ScriptWidth, ScriptHeight,
                gameWithBorders.W, gameWithBorders.H,
                scaleMethod.Rate);
            var gameWithoutBordersAndNotch = ApplyNotchOffset(gameWithoutBorders, gameWithBorders.X);

            GameAreaManager.GameArea = gameWithoutBordersAndNotch;

            if (scaleMethod.ByWidth)
            {
                GameAreaManager.ScriptDimension = (true, ScriptWidth);
                GameAreaManager.CompareDimension = (true, ImageWidth);
            }
            else
            {
                GameAreaManager.ScriptDimension = (false, ScriptHeight);
                GameAreaManager.CompareDimension = (false, ImageHeight);
            }
        }
    }
}