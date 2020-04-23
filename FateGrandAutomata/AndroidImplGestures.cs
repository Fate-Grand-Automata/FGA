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

            if (Build.VERSION.SdkInt >= BuildVersionCodes.O)
            {
                // Android 8+ detected, we can use ContinueStroke
                var swipePath = new Path();
                swipePath.MoveTo(Start.X, Start.Y);
                swipePath.LineTo(End.X, End.Y);

                var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration, true);
                gestureBuilder.AddStroke(swipeStroke);

                // keep the "finger" pressed on the end position for a while
                var holdPath = new Path();
                holdPath.MoveTo(End.X, End.Y);
                var holdStroke = swipeStroke.ContinueStroke(holdPath, swipeDuration, holdDuration, false);
                gestureBuilder.AddStroke(holdStroke);
            }
            else
            {
                const double fraction = 1 / 1.5;

                // Android 7 does not support ContinueStroke, so the only solution is to swipe for a fraction of the intended length
                var x = Start.X + (End.X - Start.X) * fraction;
                var y = Start.Y + (End.Y - Start.Y) * fraction;
                var end = new Location(x.Round(), y.Round());

                var swipePath = new Path();
                swipePath.MoveTo(Start.X, Start.Y);
                swipePath.LineTo(end.X, end.Y);

                var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration);
                gestureBuilder.AddStroke(swipeStroke);
            }

            PerformGesture(gestureBuilder.Build());

            const double scrollWaitTime = 0.7;
            AutomataApi.Wait(scrollWaitTime);
        }

        public void Click(Location Location)
        {
            const int duration = 50;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));
            
            PerformGesture(gestureBuilder.Build());
            
            const double clickWaitTime = 0.3;
            AutomataApi.Wait(clickWaitTime);
        }

        readonly ManualResetEventSlim _gestureWaitHandle = new ManualResetEventSlim();

        void PerformGesture(GestureDescription Gesture)
        {
            _gestureWaitHandle.Reset();

            _accessibilityService.DispatchGesture(Gesture, new GestureCompletedCallback(_gestureWaitHandle), null);

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

                PerformGesture(gestureBuilder.Build());
            }
        }
    }
}
