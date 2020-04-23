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

            if (Build.VERSION.SdkInt >= BuildVersionCodes.O)
            {
                // Android 8+ detected, we can use ContinueStroke
                var swipePath = new Path();
                swipePath.MoveTo(Start.X, Start.Y);
                swipePath.LineTo(End.X, End.Y);
                var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration, true);
                PerformGesture(swipeStroke);

                // keep the "finger" pressed on the end position for a while
                var holdPath = new Path();
                holdPath.MoveTo(End.X, End.Y);
                var holdStroke = swipeStroke.ContinueStroke(holdPath, 0, holdDuration, false);
                PerformGesture(holdStroke);
            }
            else
            {
                // Android 7 does not support ContinueStroke, so the only solution is to swipe for a fraction of the intended length
                const double fraction = 1 / 1.5;
                var x = Start.X + (End.X - Start.X) * fraction;
                var y = Start.Y + (End.Y - Start.Y) * fraction;
                var end = new Location(x.Round(), y.Round());

                // Let's swipe a bit to the left so that any servant loses focus
                end.X = 5;

                var swipePath = new Path();
                swipePath.MoveTo(Start.X, Start.Y);
                swipePath.LineTo(end.X, end.Y);
                var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration);
                PerformGesture(swipeStroke);
            }

            const double scrollWaitTime = 0.7;
            AutomataApi.Wait(scrollWaitTime);
        }

        const double clickWaitTime = 0.3;

        public void Click(Location Location)
        {
            const int duration = 50;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);
            PerformGesture(new GestureDescription.StrokeDescription(swipePath, 0, duration));
            
            AutomataApi.Wait(clickWaitTime);
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
                PerformGesture(stroke);
            }

            AutomataApi.Wait(clickWaitTime);
        }

        readonly ManualResetEventSlim _gestureWaitHandle = new ManualResetEventSlim();

        void PerformGesture(GestureDescription.StrokeDescription StrokeDescription)
        {
            _gestureWaitHandle.Reset();

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(StrokeDescription);

            _accessibilityService.DispatchGesture(gestureBuilder.Build(), new GestureCompletedCallback(_gestureWaitHandle), null);

            _gestureWaitHandle.Wait();
        }
    }
}
