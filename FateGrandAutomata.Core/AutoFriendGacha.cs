using CoreAutomata;

namespace FateGrandAutomata
{
    // Not Tested
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
                AutomataApi.Wait(3);

                AutomataApi.ContinueClick(new Location(1600, 1300), 15);
                AutomataApi.Wait(0.5);
            }
        }
    }
}