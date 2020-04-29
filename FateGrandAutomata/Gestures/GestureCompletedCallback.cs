using System.Threading;
using Android.AccessibilityServices;

namespace FateGrandAutomata
{
    class GestureCompletedCallback : AccessibilityService.GestureResultCallback
    {
        readonly ManualResetEventSlim _event;

        public GestureCompletedCallback(ManualResetEventSlim Event)
        {
            _event = Event;
        }

        public override void OnCompleted(GestureDescription GestureDescription)
        {
            _event.Set();
            base.OnCompleted(GestureDescription);
        }

        public override void OnCancelled(GestureDescription GestureDescription)
        {
            _event.Set();
            base.OnCancelled(GestureDescription);
        }
    }
}