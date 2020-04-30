using System;
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
        }

        public void Click(Location Location)
        {
            ContinueClick(Location, 1);
        }

        public void ContinueClick(Location Location, int Times)
        {
            Times = Math.Max(1, Times / 5);
            const int clickDuration = 1;

            while (Times-- > 0)
            {
                //_superUser.SendCommand($"{InputCommand} tap {Location.X} {Location.Y}");
                _superUser.SendCommand($"{InputCommand} swipe {Location.X} {Location.Y} {Location.X} {Location.Y} {clickDuration}");
            }
        }

        public void Dispose() { }
    }
}