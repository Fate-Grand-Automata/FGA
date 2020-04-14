using Android.Content;
using Android.OS;
using Android.Util;
using Android.Views;
using CoreAutomata;
using Java.Interop;

namespace FateGrandAutomata
{
    public static class CutoutManager
    {
        static bool _cutoutFound;

        static (int L, int T, int R, int B)? _cutoutVal;

        public static void ApplyCutout(MainActivity MainActivity)
        {
            if (_cutoutFound)
                return;

            // Android P added support for display cutouts
            if (Build.VERSION.SdkInt < BuildVersionCodes.P)
                return;

            var cutout = MainActivity.Window.DecorView.RootWindowInsets.DisplayCutout;
            if (cutout == null)
                return;

            var l = cutout.SafeInsetLeft;
            var t = cutout.SafeInsetTop;
            var r = cutout.SafeInsetRight;
            var b = cutout.SafeInsetBottom;

            // Check if there is a cutout
            if (!(l == 0 && t == 0 && r == 0 && b == 0))
            {
                var wm = MainActivity.GetSystemService(Context.WindowService).JavaCast<IWindowManager>();
                var rotation = wm.DefaultDisplay.Rotation;

                // Store the cutout for Portrait orientation of device
                switch (rotation)
                {
                    case SurfaceOrientation.Rotation90:
                        (l, t, r, b) = (b, l, t, r);
                        break;

                    case SurfaceOrientation.Rotation180:
                        (l, t, r, b) = (r, b, l, t);
                        break;

                    case SurfaceOrientation.Rotation270:
                        (l, t, r, b) = (t, r, b, l);
                        break;
                }

                _cutoutVal = (l, t, r, b);
            }

            _cutoutFound = true;
        }

        static (int L, int T, int R, int B)? GetCutout(SurfaceOrientation Rotation)
        {
            if (!GameAreaManager.AutoGameArea)
                return null;

            var cutout = _cutoutVal;

            if (cutout == null)
                return null;

            var (l, t, r, b) = cutout.Value;

            // Consider current orientation of screen
            switch (Rotation)
            {
                case SurfaceOrientation.Rotation90:
                    (l, t, r, b) = (t, r, b, l);
                    break;

                case SurfaceOrientation.Rotation180:
                    (l, t, r, b) = (r, b, l, t);
                    break;

                case SurfaceOrientation.Rotation270:
                    (l, t, r, b) = (b, l, t, r);
                    break;
            }

            return (l, t, r, b);
        }

        public static Region GetCutoutAppliedRegion(Context Context)
        {
            var metrics = new DisplayMetrics();
            var wm = Context.GetSystemService(Context.WindowService).JavaCast<IWindowManager>();

            wm.DefaultDisplay.GetRealMetrics(metrics);

            var w = metrics.WidthPixels;
            var h = metrics.HeightPixels;
            var x = 0;
            var y = 0;

            var cutout = GetCutout(wm.DefaultDisplay.Rotation);

            if (cutout != null)
            {
                var (l, t, r, b) = cutout.Value;

                x = l;
                y = t;
                w -= l + r;
                h -= t + b;
            }

            return new Region(x, y, w, h);
        }
    }
}