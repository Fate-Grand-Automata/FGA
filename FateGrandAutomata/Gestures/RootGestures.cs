using CoreAutomata;

namespace FateGrandAutomata
{
    public class RootGestures : IGestureService
    {
        readonly SuperUser _superUser;

        public RootGestures(SuperUser SuperUser)
        {
            _superUser = SuperUser;
        }

        const string InputCommand = "/system/bin/input";

        public void Swipe(Location Start, Location End)
        {
            _superUser.SendCommand($"{InputCommand} swipe {Start.X} {Start.Y} {End.X} {End.Y} {GestureTimings.SwipeDurationMs}");

            AutomataApi.Wait(GestureTimings.SwipeWaitTimeSec);
        }

        public void Click(Location Location)
        {
            ContinueClick(Location, 1);
        }

        public void ContinueClick(Location Location, int Times)
        {
            while (Times-- > 0)
            {
                AutomataApi.Wait(GestureTimings.ClickDelayMs);

                _superUser.SendCommand($"{InputCommand} tap {Location.X} {Location.Y}");
            }

            AutomataApi.Wait(GestureTimings.ClickWaitTimeSec);
        }

        public void Dispose() { }
    }
}