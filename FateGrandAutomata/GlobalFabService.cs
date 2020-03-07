using Android;
using Android.AccessibilityServices;
using Android.App;
using Android.Content;
using Android.Graphics;
using Android.Media.Projection;
using Android.Views;
using Android.Views.Accessibility;
using Android.Widget;
using Java.Interop;

namespace FateGrandAutomata
{
    [Service(Permission = Manifest.Permission.BindAccessibilityService)]
    [IntentFilter(new [] { "android.accessibilityservice.AccessibilityService" })]
    [MetaData("android.accessibilityservice", Resource = "@xml/global_fab_service")]
    public class GlobalFabService : AccessibilityService
    {
        FrameLayout _layout;
        FabServiceBroadcastReceiver _broadcastReceiver;
        bool _fabVisible;

        MediaProjectionManager _mediaProjectionManager;
        MediaProjection _mediaProjection;

        protected override void OnServiceConnected()
        {
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

            _broadcastReceiver.MediaProjectionToken += Token =>
            {
                _mediaProjection = _mediaProjectionManager.GetMediaProjection((int) Result.Ok, Token);
            };
        }

        public override void OnAccessibilityEvent(AccessibilityEvent e)
        {
        }

        public override void OnInterrupt()
        {
        }
    }
}