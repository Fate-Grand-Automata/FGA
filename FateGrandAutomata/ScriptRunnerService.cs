using System;
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
using AndroidX.Core.App;
using CoreAutomata;
using Java.Interop;

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
                _mediaProjection = MediaProjectionManager.GetMediaProjection((int)Result.Ok, MediaProjectionToken);
            }

            SetupVirtualDisplay();

            _windowManager.AddView(_layout, _layoutParams);
            ServiceStarted = true;

            ShowForegroundNotification();

            return true;
        }

        public bool Stop()
        {
            StopScript();

            if (!ServiceStarted)
            {
                return false;
            }

            _virtualDisplay?.Release();
            _virtualDisplay = null;

            _mediaProjection?.Stop();
            _mediaProjection = null;

            _windowManager.RemoveView(_layout);
            ServiceStarted = false;

            HideForegroundNotification();

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
            ScriptMode.SupportImageMaker => new SupportImageMaker(),
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

            _entryPoint = GetEntryPoint();
            _entryPoint.ScriptExit += OnScriptExit;

            SetScriptControlBtnIcon(Resource.Drawable.ic_stop);

            _entryPoint.Run();

            _scriptStarted = true;

            ShowStatusNotification("Script Running");
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

            ShowStatusNotification("Ready");
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
                Gravity = GravityFlags.Left | GravityFlags.Top,
                X = 0,
                Y = 0
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

            _scriptCtrlBtn.Touch += ScriptCtrlBtnOnTouch;

            MediaProjectionManager = (MediaProjectionManager)GetSystemService(MediaProjectionService);

            var metrics = new DisplayMetrics();
            _windowManager.DefaultDisplay.GetRealMetrics(metrics);
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
        }

        void ScriptCtrlBtnOnTouch(object S, View.TouchEventArgs E)
        {
            switch (E.Event.ActionMasked)
            {
                case MotionEventActions.Down:
                    _dX = _layoutParams.X - E.Event.RawX;
                    _dY = _layoutParams.Y - E.Event.RawY;
                    _lastAction = MotionEventActions.Down;

                    E.Handled = false;
                    break;

                case MotionEventActions.Move:
                    var newX = E.Event.RawX + _dX;
                    var newY = E.Event.RawY + _dY;

                    var d = Math.Sqrt(Math.Pow(newX - _layoutParams.X, 2) + Math.Pow(newY - _layoutParams.Y, 2));

                    if (d > DragThreshold)
                    {
                        _layoutParams.X = (int) Math.Round(newX);
                        _layoutParams.Y = (int) Math.Round(newY);

                        _windowManager.UpdateViewLayout(_layout, _layoutParams);

                        _lastAction = MotionEventActions.Move;
                    }

                    E.Handled = true;
                    break;

                case MotionEventActions.Up:
                    E.Handled = _lastAction == MotionEventActions.Move;
                    break;
            }
        }

        float _dX, _dY;
        MotionEventActions _lastAction;
        const int DragThreshold = 10;

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

        const string ChannelId = "fategrandautomata-notifications";
        bool _channelCreated;

        void CreateNotificationChannel()
        {
            if (_channelCreated)
                return;

            if (Build.VERSION.SdkInt >= BuildVersionCodes.O)
            {
                var channel = new NotificationChannel(ChannelId,
                    ChannelId,
                    NotificationImportance.Max)
                {
                    Description = ChannelId
                };

                var notifyManager = NotificationManagerCompat.From(this);

                notifyManager.CreateNotificationChannel(channel);
            }

            _channelCreated = true;
        }

        void HideForegroundNotification() => StopForeground(true);

        NotificationCompat.Builder StartBuildNotification()
        {
            CreateNotificationChannel();

            var activityIntent = PendingIntent
                .GetActivity(this, 0, new Intent(this, typeof(MainActivity)), 0);

            return new NotificationCompat.Builder(this, ChannelId)
                .SetOngoing(true)
                .SetContentTitle(GetString(Resource.String.app_name))
                .SetContentText("Accessibility Service Running")
                .SetSmallIcon(Resource.Mipmap.ic_launcher)
                .SetPriority(NotificationCompat.PriorityMax)
                .SetContentIntent(activityIntent);
        }

        const int ForegroundNotificationId = 1;

        public void ShowStatusNotification(string Msg)
        {
            var builder = StartBuildNotification()
                .SetContentText(Msg)
                .SetStyle(new NotificationCompat
                    .BigTextStyle()
                    .BigText(Msg));

            NotificationManagerCompat
                .From(this)
                .Notify(ForegroundNotificationId, builder.Build());
        }

        void ShowForegroundNotification()
        {
            var builder = StartBuildNotification();

            StartForeground(ForegroundNotificationId, builder.Build());
        }
    }
}
