using System;
using System.IO;
using System.Threading;
using Android.AccessibilityServices;
using Android.OS;
using Android.Widget;
using CoreAutomata;
using Org.Opencv.Android;
using Environment = Android.OS.Environment;
using Path = Android.Graphics.Path;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        readonly ScriptRunnerService _accessibilityService;

        public AndroidImpl(ScriptRunnerService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;

            OpenCVLoader.InitDebug();
        }

        public Region WindowRegion => CutoutManager.GetCutoutAppliedRegion(_accessibilityService);

        public void Scroll(Location Start, Location End)
        {
            const int duration = 300;

            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);
            swipePath.LineTo(End.X, End.Y);
            
            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));
            
            PerformGesture(gestureBuilder.Build());
        }

        readonly Lazy<Handler> _handler = new Lazy<Handler>(() => new Handler(Looper.MainLooper));

        public void Toast(string Msg)
        {
            _handler.Value.Post(() =>
                Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short).Show());
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

        public IPattern Screenshot()
        {
            return ScriptRunnerService
                .Instance
                .AcquireLatestImage();
        }

        public IPattern LoadPattern(Stream Stream)
        {
            return new DroidCvPattern(Stream);
        }

        public void MessageBox(string Title, string Message)
        {
            _handler.Value.Post(() =>
            {
                var msg = $"{Title.ToUpper()}: {Message}";

                Android.Widget.Toast
                    .MakeText(_accessibilityService, msg, ToastLength.Long)
                    .Show();

                _accessibilityService.ShowStatusNotification(msg);
            });
        }

        public string StorageRootDir => Environment.ExternalStorageDirectory.AbsolutePath;
    }
}