using System.Threading;
using Android.AccessibilityServices;
using Android.Graphics;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class AccessibilityGestureService : IGestureService
    {
        AccessibilityService _accessibilityService;
        readonly ManualResetEventSlim _gestureWaitHandle = new ManualResetEventSlim();

        public AccessibilityGestureService(AccessibilityService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;
        }

        public void Swipe(Location Start, Location End)
        {
            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);
            swipePath.LineTo(End.X, End.Y);
            var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, GestureTimings.SwipeDurationMs);
            PerformGesture(swipeStroke);

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
                var swipePath = new Path();
                swipePath.MoveTo(Location.X, Location.Y);

                var stroke = new GestureDescription.StrokeDescription(swipePath, GestureTimings.ClickDelayMs, GestureTimings.ClickDurationMs);
                PerformGesture(stroke);
            }

            AutomataApi.Wait(GestureTimings.ClickWaitTimeSec);
        }
        
        void PerformGesture(GestureDescription.StrokeDescription StrokeDescription)
        {
            _gestureWaitHandle.Reset();

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(StrokeDescription);

            _accessibilityService.DispatchGesture(gestureBuilder.Build(), new GestureCompletedCallback(_gestureWaitHandle), null);

            _gestureWaitHandle.Wait();
        }

        public void Dispose()
        {
            _accessibilityService = null;
        }
    }
}
