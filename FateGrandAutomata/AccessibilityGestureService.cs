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

        const double ClickWaitTime = 0.3;

        public AccessibilityGestureService(AccessibilityService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;
        }

        public void Scroll(Location Start, Location End)
        {
            const int swipeDuration = 300;

            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);
            swipePath.LineTo(End.X, End.Y);
            var swipeStroke = new GestureDescription.StrokeDescription(swipePath, 0, swipeDuration);
            PerformGesture(swipeStroke);

            const double scrollWaitTime = 0.7;
            AutomataApi.Wait(scrollWaitTime);
        }

        public void Click(Location Location)
        {
            const int duration = 50;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);
            PerformGesture(new GestureDescription.StrokeDescription(swipePath, 0, duration));
            
            AutomataApi.Wait(ClickWaitTime);
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

            AutomataApi.Wait(ClickWaitTime);
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
