using System;
using System.Collections.Generic;
using Android.AccessibilityServices;
using Android.Content;
using Android.Graphics;
using Android.Util;
using Android.Views;
using Android.Widget;
using Java.Interop;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        readonly AccessibilityService _accessibilityService;

        public AndroidImpl(AccessibilityService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;
        }

        public (int Width, int Height) WindowSize
        {
            get
            {
                var metrics = new DisplayMetrics();
                var wm = _accessibilityService.GetSystemService(Context.WindowService).JavaCast<IWindowManager>();

                wm.DefaultDisplay.GetMetrics(metrics);

                return (metrics.WidthPixels, metrics.HeightPixels);
            }
        }

        public void UseSameSnapIn(Action Action)
        {
            throw new NotImplementedException();
        }

        public T UseSameSnapIn<T>(Func<T> Action)
        {
            throw new NotImplementedException();
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

        public IEnumerable<Region> FindAll(Pattern Pattern)
        {
            throw new NotImplementedException();
        }

        public IEnumerable<Region> FindAll(Region Region, Pattern Pattern)
        {
            throw new NotImplementedException();
        }

        public void Toast(string Msg)
        {
            Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short);
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

        public void WaitVanish(Region Region, Pattern Image, int? Timeout = null)
        {
            throw new NotImplementedException();
        }

        public bool Exists(Region Region, Pattern Image, int? Timeout = null, double? Similarity = null)
        {
            throw new NotImplementedException();
        }

        public Pattern Save(Region Region)
        {
            throw new NotImplementedException();
        }
    }
}