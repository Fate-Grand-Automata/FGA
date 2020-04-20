using System.Threading;
using Android.AccessibilityServices;
using Android.Graphics;
using CoreAutomata;

namespace FateGrandAutomata
{
    public partial class AndroidImpl
    {
        public void Scroll(Location Start, Location End)
        {
            const int duration = 300;

            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);

            var center = new Location((Start.X + End.X) / 2, (Start.Y + End.Y) / 2);
            swipePath.LineTo(center.X, center.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));

            PerformGesture(gestureBuilder.Build());
        }

        public void Click(Location Location)
        {
            const int duration = 1;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));

            PerformGesture(gestureBuilder.Build());
        }

        readonly ManualResetEventSlim _gestureWaitHandle = new ManualResetEventSlim();

        void PerformGesture(GestureDescription Gesture, bool DoWait = true)
        {
            _gestureWaitHandle.Reset();

            _accessibilityService.DispatchGesture(Gesture, new GestureCompletedCallback(_gestureWaitHandle), null);

            if (DoWait)
            {
                AutomataApi.Wait(GestureWait);
            }

            _gestureWaitHandle.Wait();
        }

        const double GestureWait = 0.3;

        public void ContinueClick(Location Location, int Times)
        {
            const int clickTime = 50;
            const int clickDelay = 10;

            while (Times-- > 0)
            {
                var swipePath = new Path();
                swipePath.MoveTo(Location.X, Location.Y);

                var stroke = new GestureDescription.StrokeDescription(swipePath, clickDelay, clickTime);

                var gestureBuilder = new GestureDescription.Builder()
                    .AddStroke(stroke);

                PerformGesture(gestureBuilder.Build(), false);
            }
        }
    }
}