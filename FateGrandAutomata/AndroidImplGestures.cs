using System.Threading;
using Android.AccessibilityServices;
using Android.Graphics;
using Android.OS;
using CoreAutomata;

namespace FateGrandAutomata
{
    public partial class AndroidImpl
    {
        public void Scroll(Location Start, Location End)
        {
            const int swipeDuration = 300;
            const int holdDuration = 300;

            var gestureBuilder = new GestureDescription.Builder();

            //if (Build.VERSION.SdkInt >= BuildVersionCodes.O)
            //{
            //    // Android 8+ detected, we can use ContinueStroke
            //    var swipePath = new Path();
            //    swipePath.MoveTo(Start.X, Start.Y);
            //    swipePath.LineTo(End.X, End.Y);

            //    var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration, true);
            //    gestureBuilder.AddStroke(swipeStroke);

            //    // keep the "finger" pressed on the end position for a while
            //    var holdPath = new Path();
            //    holdPath.MoveTo(End.X, End.Y);
            //    var holdStroke = swipeStroke.ContinueStroke(holdPath, swipeDuration, holdDuration, false);
            //    gestureBuilder.AddStroke(holdStroke);
            //}
            //else
            {
                // Android 7 does not support ContinueStroke, so the only solution is to swipe for half of the intended length
                var center = new Location((Start.X + End.X) / 2, (Start.Y + End.Y) / 2);

                var swipePath = new Path();
                swipePath.MoveTo(Start.X, Start.Y);
                swipePath.LineTo(center.X, center.Y);

                var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration);
                gestureBuilder.AddStroke(swipeStroke);
            }

            const double scrollWaitTime = 0.7;
            PerformGesture(gestureBuilder.Build(), scrollWaitTime);
        }

        public void Click(Location Location)
        {
            const int duration = 1;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));

            const double clickWaitTime = 0.4;
            PerformGesture(gestureBuilder.Build(), clickWaitTime);
        }

        readonly ManualResetEventSlim _gestureWaitHandle = new ManualResetEventSlim();

        void PerformGesture(GestureDescription Gesture, double WaitTime)
        {
            _gestureWaitHandle.Reset();

            _accessibilityService.DispatchGesture(Gesture, new GestureCompletedCallback(_gestureWaitHandle), null);

            if (WaitTime > 0)
            {
                AutomataApi.Wait(WaitTime);
            }

            _gestureWaitHandle.Wait();
        }

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

                PerformGesture(gestureBuilder.Build(), 0);
            }
        }
    }
}
