using System;
using System.IO;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class SupportImageMaker : EntryPoint
    {   
        protected override void Script()
        {
            Scaling.Init();

            var isInSupport = AutoBattle.IsInSupport();

            var supportBound = new Region(53 * 2, 0, 143 * 2, 110 * 2);
            var regionAnchor = ImageLocator.SupportRegionTool;
            var regionArray = AutomataApi.FindAll(new Region(2100, 0, 300, 1440), regionAnchor);
            var screenBounds = new Region(0, 0, Game.ScriptWidth, Game.ScriptHeight);

            var timestamp = DateTime.Now.Ticks;

            var i = 0;

            foreach (var testRegion in regionArray)
            {
                // At max two Servant+CE are completely on screen
                if (i > 1)
                    break;

                if (isInSupport)
                {
                    supportBound.Y = testRegion.Y - 70 + 68 * 2;
                }
                else // Assume we are on Friend List
                {
                    supportBound.Y = testRegion.Y + 82;
                    supportBound.X += 10;
                }

                if (!screenBounds.Contains(supportBound))
                    continue;

                using var pattern = supportBound.GetPattern();

                var servant = pattern.Crop(new Region(0, 0, 125, 44));
                servant.Save(Path.Combine(ImageLocator.SupportServantImgFolder, $"{timestamp}_servant{i}.png"));

                var ce = pattern.Crop(new Region(0, 80, pattern.Width, 25));
                ce.Save(Path.Combine(ImageLocator.SupportCeImgFolder, $"{timestamp}_ce{i}.png"));

                ++i;
            }

            if (i == 0)
            {
                throw new ScriptExitException("No support images were found on the current screen. Are you on Support selection or Friend list screen?");
            }
            
            throw new ScriptExitException($"Support Image(s) were generated.");
        }
    }
}