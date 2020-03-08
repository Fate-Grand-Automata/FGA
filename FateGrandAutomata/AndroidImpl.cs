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
using Path = Android.Graphics.Path;
using Region = CoreAutomata.Region;
using Size = CoreAutomata.Size;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        readonly AccessibilityService _accessibilityService;

        public AndroidImpl(AccessibilityService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;
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
            const int Duration = 500;

            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);
            swipePath.LineTo(End.X, End.Y);
            
            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, Duration));
            
            _accessibilityService.DispatchGesture(gestureBuilder.Build(), null, null);
        }

        readonly Lazy<Handler> _handler = new Lazy<Handler>(() => new Handler(Looper.MainLooper));

        public void Toast(string Msg)
        {
            _handler.Value.Post(() =>
                Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short).Show());
        }

        public void Click(Location Location)
        {
            const int Duration = 1;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, Duration));

            _accessibilityService.DispatchGesture(gestureBuilder.Build(), null, null);
        }

        public void ContinueClick(Location Location, int Times, int Timeout = -1)
        {
            throw new NotImplementedException();
        }

        public IPattern Screenshot(Region Region, Size? TargetSize = null)
        {
            throw new NotImplementedException();
        }

        public IPattern LoadPattern(Stream Stream, Size? TargetSize = null)
        {
            throw new NotImplementedException();
        }
    }
}