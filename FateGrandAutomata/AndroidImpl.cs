using System;
using System.IO;
using Android.AccessibilityServices;
using Android.Content;
using Android.OS;
using Android.Util;
using Android.Views;
using Android.Widget;
using CoreAutomata;
using Java.Interop;
using Org.Opencv.Android;
using Path = Android.Graphics.Path;
using Size = CoreAutomata.Size;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        readonly AccessibilityService _accessibilityService;

        public AndroidImpl(AccessibilityService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;

            OpenCVLoader.InitDebug();
        }

        public Size WindowSize
        {
            get
            {
                var metrics = new DisplayMetrics();
                var wm = _accessibilityService.GetSystemService(Context.WindowService).JavaCast<IWindowManager>();

                wm.DefaultDisplay.GetMetrics(metrics);

                return new Size(metrics.WidthPixels, metrics.HeightPixels);
            }
        }

        public void Scroll(Location Start, Location End)
        {
            const int delay = 500;
            const int duration = 1000;

            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);
            swipePath.LineTo(End.X, End.Y);
            
            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, delay, duration));
            
            _accessibilityService.DispatchGesture(gestureBuilder.Build(), null, null);

            AutomataApi.Wait(2);
        }

        readonly Lazy<Handler> _handler = new Lazy<Handler>(() => new Handler(Looper.MainLooper));

        public void Toast(string Msg)
        {
            _handler.Value.Post(() =>
                Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short).Show());
        }

        public void Click(Location Location)
        {
            const int delay = 1000;
            const int duration = 1;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, delay, duration));

            _accessibilityService.DispatchGesture(gestureBuilder.Build(), null, null);

            AutomataApi.Wait(1.5);
        }

        public void ContinueClick(Location Location, int Times, int Timeout = -1)
        {
            Click(Location);

            AutomataApi.WriteDebug($"{nameof(ContinueClick)} not implemented");
        }

        public IPattern Screenshot()
        {
            return GlobalFabService
                .Instance
                .AcquireLatestImage();
        }

        public IPattern LoadPattern(Stream Stream)
        {
            return new DroidCvPattern(Stream);
        }
    }
}