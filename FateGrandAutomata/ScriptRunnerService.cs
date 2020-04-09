using System.IO;
using Android;
using Android.AccessibilityServices;
using Android.App;
using Android.Content;
using Android.Graphics;
using Android.Hardware.Display;
using Android.Media;
using Android.Media.Projection;
using Android.OS;
using Android.Util;
using Android.Views;
using Android.Views.Accessibility;
using Android.Widget;
using CoreAutomata;
using Java.Interop;
using Path = System.IO.Path;

namespace FateGrandAutomata
{
    [Service(Permission = Manifest.Permission.BindAccessibilityService)]
    [IntentFilter(new [] { "android.accessibilityservice.AccessibilityService" })]
    [MetaData("android.accessibilityservice", Resource = "@xml/script_runner_service")]
    public class ScriptRunnerService : AccessibilityService
    {
        FrameLayout _layout;
        IWindowManager _windowManager;
        WindowManagerLayoutParams _layoutParams;

        int _screenDensity, _screenWidth, _screenHeight;

        public MediaProjectionManager MediaProjectionManager { get; private set; }
        MediaProjection _mediaProjection;
        VirtualDisplay _virtualDisplay;
        ImageReader _imageReader;
        ImgListener _imgListener;
        Intent _mediaProjectionToken;

        public static ScriptRunnerService Instance { get; private set; }

        public override bool OnUnbind(Intent Intent)
        {
            Stop();

            Instance = null;
            ServiceStarted = false;

            _imageReader?.Close();
            _imageReader = null;

            return base.OnUnbind(Intent);
        }

        void StopMediaProjection()
        {
            _virtualDisplay?.Release();
            _virtualDisplay = null;

            _mediaProjection?.Stop();
            _mediaProjection = null;
        }

        public bool HasMediaProjectionToken => _mediaProjectionToken != null;

        public bool ServiceStarted { get; private set; }

        bool _scriptStarted;

        void StartMediaProjection()
        {
            _mediaProjection = MediaProjectionManager.GetMediaProjection((int)Result.Ok, _mediaProjectionToken);

            SetupVirtualDisplay();
        }

        public bool Start(Intent MediaProjectionToken = null)
        {
            if (ServiceStarted)
            {
                return false;
            }

            if (MediaProjectionToken != null)
            {
                _mediaProjectionToken = MediaProjectionToken;
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

        void SetScriptControlBtnIcon(int IconId)
        {
            _scriptCtrlBtn.SetCompoundDrawablesWithIntrinsicBounds(GetDrawable(IconId),
                null, null, null);
        }

        void OnScriptExit(string Message = null)
        {
            _scriptCtrlBtn.Post(() =>
            {
                SetScriptControlBtnIcon(Resource.Drawable.ic_play);
            });
            
            _entryPoint = null;

            _scriptStarted = false;
        }

        static EntryPoint GetEntryPoint() => Preferences.Instance.ScriptMode switch
        {
            ScriptMode.Lottery => new AutoLottery(),
            ScriptMode.FriendGacha => new AutoFriendGacha(),
            _ => new AutoBattle()
        };

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

            StartMediaProjection();

            _entryPoint = GetEntryPoint();
            _entryPoint.ScriptExit += OnScriptExit;

            SetScriptControlBtnIcon(Resource.Drawable.ic_stop);

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

            StopMediaProjection();

            OnScriptExit();
        }

        Button _scriptCtrlBtn;

        protected override void OnServiceConnected()
        {
            Instance = this;

            AutomataApi.RegisterPlatform(new AndroidImpl(this));
            Preferences.SetPreference(new FgoPreferences(this));

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
            inflator.Inflate(Resource.Layout.script_runner, _layout);

            _scriptCtrlBtn = _layout.FindViewById<Button>(Resource.Id.script_toggle_btn);

            _scriptCtrlBtn.Click += (S, E) =>
            {
                if (_scriptStarted)
                {
                    StopScript();
                }
                else StartScript();
            };

            MediaProjectionManager = (MediaProjectionManager)GetSystemService(MediaProjectionService);

            var metrics = new DisplayMetrics();
            _windowManager.DefaultDisplay.GetMetrics(metrics);
            _screenDensity = (int)metrics.DensityDpi;
            _screenWidth = metrics.WidthPixels;
            _screenHeight = metrics.HeightPixels;

            // Retrieve images in Landscape
            if (_screenHeight > _screenWidth)
            {
                (_screenWidth, _screenHeight) = (_screenHeight, _screenWidth);
            }

            _imageReader = ImageReader.NewInstance(_screenWidth, _screenHeight, (ImageFormatType)1, 2);
            _imgListener = new ImgListener(_imageReader);
            _imageReader.SetOnImageAvailableListener(_imgListener, null);

            ImageLocator.FileLoader = FileLoader;
        }

        static System.IO.Stream FileLoader(string Filename)
        {
            PrepareSupportImageFolder();

            var filepath = Path.Combine(GetSupportImgFolder(), Filename);

            return File.Exists(filepath)
                ? File.OpenRead(filepath)
                : null;
        }

        static string GetSupportImgFolder() =>
            Path.Combine(Environment.ExternalStorageDirectory.AbsolutePath, ImageLocator.SupportImageFolderName);

        static void PrepareSupportImageFolder()
        {
            var folder = GetSupportImgFolder();

            if (!Directory.Exists(folder))
            {
                Directory.CreateDirectory(folder);
            }
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