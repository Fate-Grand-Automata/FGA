using CoreAutomata;

namespace FateGrandAutomata
{
    public class AutoLottery : EntryPoint
    {
        static readonly Location SpinClick = new Location(834, 860);

        static readonly Region FinishedLotteryBoxRegion = new Region(575, 860, 70, 100);

        static readonly Region FullPresentBoxRegion = new Region(1280, 720, 1280, 720);
        
        static readonly Location ResetClick = new Location(2200, 480);

        static readonly Location ResetConfirmationClick = new Location(1774, 1122);

        static readonly Location ResetCloseClick = new Location(1270, 1120);

        void Spin()
        {
            SpinClick.ContinueClick(480);
        }

        void Reset()
        {
            ResetClick.Click();
            AutomataApi.Wait(0.5);

            ResetConfirmationClick.Click();
            AutomataApi.Wait(3);

            ResetCloseClick.Click();
            AutomataApi.Wait(2);
        }

        protected override void Script()
        {
            Scaling.Init();

            while (true)
            {
                if (FinishedLotteryBoxRegion.Exists(ImageLocator.FinishedLotteryBox, Similarity: 0.65))
                {
                    Reset();
                }
                else if (FullPresentBoxRegion.Exists(ImageLocator.PresentBoxFull))
                {
                    throw new ScriptExitException("Present Box Full");
                }
                else Spin();
            }
        }
    }
}