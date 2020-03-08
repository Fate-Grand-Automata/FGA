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
using Java.Interop;

namespace FateGrandAutomata
{
    [Service(Permission = Manifest.Permission.BindAccessibilityService)]
    [IntentFilter(new [] { "android.accessibilityservice.AccessibilityService" })]
    [MetaData("android.accessibilityservice", Resource = "@xml/global_fab_service")]
    public class GlobalFabService : AccessibilityService, IGlobalFab
    {
        FrameLayout _layout;
        FabServiceBroadcastReceiver _broadcastReceiver;
        bool _fabVisible;

        int _screenDensity, _screenWidth, _screenHeight;

        MediaProjectionManager _mediaProjectionManager;
        MediaProjection _mediaProjection;
        VirtualDisplay _virtualDisplay;
        ImageReader _imageReader;

        protected override void OnServiceConnected()
        {
            Game.Impl = new AndroidImpl(this);

            var wm = GetSystemService(WindowService).JavaCast<IWindowManager>();

            _layout = new FrameLayout(this);
            var lp = new WindowManagerLayoutParams
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

            _broadcastReceiver = new FabServiceBroadcastReceiver();
            var intentFilter = _broadcastReceiver.CreateIntentFilter();

            RegisterReceiver(_broadcastReceiver, intentFilter);

            _broadcastReceiver.ToggleService += () =>
            {
                if (_fabVisible)
                {
                    wm.RemoveView(_layout);
                    _fabVisible = false;
                }
                else
                {
                    wm.AddView(_layout, lp);
                    _fabVisible = true;
                }
            };

            _mediaProjectionManager = (MediaProjectionManager)GetSystemService(Context.MediaProjectionService);

            var metrics = new DisplayMetrics();
            wm.DefaultDisplay.GetMetrics(metrics);
            _screenDensity = (int)metrics.DensityDpi;
            _screenWidth = metrics.WidthPixels;
            _screenHeight = metrics.HeightPixels;

            _imageReader = ImageReader.NewInstance(_screenWidth, _screenHeight, (ImageFormatType)1, 1);

            _broadcastReceiver.MediaProjectionToken += Token =>
            {
                _mediaProjection = _mediaProjectionManager.GetMediaProjection((int) Result.Ok, Token);

                SetupVirtualDisplay();
            };
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

        void TeardownMediaProjection()
        {
            if (_mediaProjection != null)
            {
                _mediaProjection.Stop();
                _mediaProjection = null;
            }
        }

        void StopScreenCapture()
        {
            if (_virtualDisplay == null)
            {
                return;
            }

            _virtualDisplay.Release();
            _virtualDisplay = null;

            _imageReader.Close();
            _imageReader = null;
        }

        void SetupVirtualDisplay()
        {
            _virtualDisplay = _mediaProjection.CreateVirtualDisplay("ScreenCapture",
                _screenWidth, _screenHeight, _screenDensity,
                DisplayFlags.None, _imageReader.Surface, null, null);
        }

        public override void OnAccessibilityEvent(AccessibilityEvent e)
        {
        }

        public override void OnInterrupt()
        {
        }

        public bool HasMediaProjectionToken => _mediaProjection != null;

        public bool IsStarted { get; private set; }
        
        public void Start(Intent MediaProjectionToken = null)
        {
            throw new System.NotImplementedException();
        }

        public void Stop()
        {
            throw new System.NotImplementedException();
        }
    }
}