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
        ImgListener _imgListener;

        public static GlobalFabService Instance { get; private set; }

        public override bool OnUnbind(Intent Intent)
        {
            Stop();

            Instance = null;
            ServiceStarted = false;

            _virtualDisplay?.Release();
            _virtualDisplay = null;

            _imageReader?.Close();
            _imageReader = null;

            _mediaProjection?.Stop();
            _mediaProjection = null;

            return base.OnUnbind(Intent);
        }

        public bool HasMediaProjectionToken => _mediaProjection != null;

        public bool ServiceStarted { get; private set; }

        bool _scriptStarted;

        public bool Start(Intent MediaProjectionToken = null)
        {
            if (ServiceStarted)
            {
                return false;
            }

            if (MediaProjectionToken != null)
            {
                _mediaProjection = _mediaProjectionManager.GetMediaProjection((int)Result.Ok, MediaProjectionToken);

                SetupVirtualDisplay();
            }

            _windowManager.AddView(_layout, _layoutParams);
            ServiceStarted = true;

            return true;
        }

        public bool Stop()
        {
            StopScript();

            if (!ServiceStarted)
            {
                return false;
            }

            _windowManager.RemoveView(_layout);
            ServiceStarted = false;

            return true;
        }

        EntryPoint _entryPoint;

        void OnScriptExit(string Message = null)
        {
            _scriptCtrlBtn.Post(() => _scriptCtrlBtn.Text = "START");
            
            _entryPoint = null;

            _scriptStarted = false;
        }

        void StartScript()
        {
            if (!ServiceStarted)
            {
                return;
            }

            if (_scriptStarted)
            {
                return;
            }

            _entryPoint = new Regular();
            _entryPoint.ScriptExit += OnScriptExit;

            _scriptCtrlBtn.Text = "STOP";
            _entryPoint.Run();

            _scriptStarted = true;
        }
        void StopScript()
        {
            if (!_scriptStarted)
            {
                return;
            }

            _entryPoint.ScriptExit -= OnScriptExit;
            _entryPoint.Stop();

            OnScriptExit();
        }

        Button _scriptCtrlBtn;

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
                Gravity = GravityFlags.Left | GravityFlags.Bottom
            };

            var inflator = LayoutInflater.From(this);
            inflator.Inflate(Resource.Layout.global_fab_layout, _layout);

            _scriptCtrlBtn = _layout.FindViewById<Button>(Resource.Id.power);

            _scriptCtrlBtn.Click += (S, E) =>
            {
                if (_scriptStarted)
                {
                    StopScript();
                }
                else StartScript();
            };

            _mediaProjectionManager = (MediaProjectionManager)GetSystemService(MediaProjectionService);

            var metrics = new DisplayMetrics();
            _windowManager.DefaultDisplay.GetMetrics(metrics);
            _screenDensity = (int)metrics.DensityDpi;
            _screenWidth = metrics.WidthPixels;
            _screenHeight = metrics.HeightPixels;

            _imageReader = ImageReader.NewInstance(_screenWidth, _screenHeight, (ImageFormatType)1, 2);
            _imgListener = new ImgListener(_imageReader);
            _imageReader.SetOnImageAvailableListener(_imgListener, null);
        }

        public IPattern AcquireLatestImage()
        {
            return _imgListener.AcquirePattern();
        }

        void SetupVirtualDisplay()
        {
            _virtualDisplay = _mediaProjection.CreateVirtualDisplay("ScreenCapture",
                _screenWidth, _screenHeight, _screenDensity,
                DisplayFlags.None, _imageReader.Surface, null, null);
        }

        public override void OnAccessibilityEvent(AccessibilityEvent E) { }

        public override void OnInterrupt() { }
    }
}