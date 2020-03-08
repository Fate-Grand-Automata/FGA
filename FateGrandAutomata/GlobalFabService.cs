using Android;
using Android.AccessibilityServices;
using Android.App;
using Android.Content;
using Android.Graphics;
using Android.Hardware.Display;
using Android.Media;
using Android.Media.Projection;
using Android.Util;
using Android.Views;
using Android.Views.Accessibility;
using Android.Widget;
using CoreAutomata;
using Java.Interop;

namespace FateGrandAutomata
{
    [Service(Permission = Manifest.Permission.BindAccessibilityService)]
    [IntentFilter(new [] { "android.accessibilityservice.AccessibilityService" })]
    [MetaData("android.accessibilityservice", Resource = "@xml/global_fab_service")]
    public class GlobalFabService : AccessibilityService
    {
        FrameLayout _layout;
        IWindowManager _windowManager;
        WindowManagerLayoutParams _layoutParams;

        int _screenDensity, _screenWidth, _screenHeight;

        MediaProjectionManager _mediaProjectionManager;
        MediaProjection _mediaProjection;
        VirtualDisplay _virtualDisplay;
        ImageReader _imageReader;

        public static GlobalFabService Instance { get; private set; }

        public override bool OnUnbind(Intent intent)
        {
            Instance = null;
            Started = false;

            _virtualDisplay.Release();
            _virtualDisplay = null;

            _imageReader.Close();
            _imageReader = null;

            if (_mediaProjection != null)
            {
                _mediaProjection.Stop();
                _mediaProjection = null;
            }

            return base.OnUnbind(intent);
        }

        public bool HasMediaProjectionToken => _mediaProjection != null;

        public bool Started { get; private set; }

        public bool Start(Intent MediaProjectionToken = null)
        {
            if (Started)
            {
                return false;
            }

            if (MediaProjectionToken != null)
            {
                _mediaProjection = _mediaProjectionManager.GetMediaProjection((int)Result.Ok, MediaProjectionToken);

                SetupVirtualDisplay();
            }

            _windowManager.AddView(_layout, _layoutParams);
            Started = true;

            return true;
        }

        public bool Stop()
        {
            if (!Started)
            {
                return false;
            }

            _windowManager.RemoveView(_layout);
            Started = false;

            return true;
        }

        protected override void OnServiceConnected()
        {
            Instance = this;

            AutomataApi.RegisterPlatform(new AndroidImpl(this));

            _windowManager = GetSystemService(WindowService).JavaCast<IWindowManager>();

            _layout = new FrameLayout(this);
            _layoutParams = new WindowManagerLayoutParams
            {
                Type = WindowManagerTypes.AccessibilityOverlay,
                Format = Format.Translucent,
                Flags = WindowManagerFlags.NotFocusable,
                Width = ViewGroup.LayoutParams.WrapContent,
                Height = ViewGroup.LayoutParams.WrapContent,
                Gravity = GravityFlags.Top
            };

            var inflator = LayoutInflater.From(this);
            inflator.Inflate(Resource.Layout.global_fab_layout, _layout);

            _mediaProjectionManager = (MediaProjectionManager)GetSystemService(Context.MediaProjectionService);

            var metrics = new DisplayMetrics();
            _windowManager.DefaultDisplay.GetMetrics(metrics);
            _screenDensity = (int)metrics.DensityDpi;
            _screenWidth = metrics.WidthPixels;
            _screenHeight = metrics.HeightPixels;

            _imageReader = ImageReader.NewInstance(_screenWidth, _screenHeight, (ImageFormatType)1, 1);
        }

        Bitmap AcquireLatestImage()
        {
            using var img = _imageReader.AcquireLatestImage();

            var planes = img.GetPlanes();
            var buffer = planes[0].Buffer;
            var pixelStride = planes[0].PixelStride;
            var rowStride = planes[0].RowStride;
            var rowPadding = rowStride - pixelStride * _screenWidth;

            var bmp = Bitmap.CreateBitmap(_screenWidth + rowPadding / pixelStride, _screenHeight, Bitmap.Config.Argb8888);

            bmp.CopyPixelsFromBuffer(buffer);

            return bmp;
        }

        void SetupVirtualDisplay()
        {
            _virtualDisplay = _mediaProjection.CreateVirtualDisplay("ScreenCapture",
                _screenWidth, _screenHeight, _screenDensity,
                DisplayFlags.None, _imageReader.Surface, null, null);
        }

        public override void OnAccessibilityEvent(AccessibilityEvent e) { }

        public override void OnInterrupt() { }
    }
}