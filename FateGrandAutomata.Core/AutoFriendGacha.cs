using System;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class AutoFriendGacha : EntryPoint
    {
        protected override void Script()
        {
            Scaling.Init();

            new Location(1400, 1120).Click();
            new Location(1600, 1120).Click();

            while (true)
            {
                new Location(1600, 1420).Click();
                new Location(1600, 1120).Click();
                TimeSpan.FromSeconds(3).Wait();

                new Location(1600, 1300).ContinueClick(15);
                TimeSpan.FromSeconds(0.5).Wait();
            }
        }
    }
}