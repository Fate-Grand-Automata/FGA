using System;
using System.IO;
using System.Threading;
using Android.AccessibilityServices;
using Android.App;
using Android.Content;
using Android.OS;
using Android.Util;
using Android.Views;
using Android.Widget;
using AndroidX.Core.App;
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
            const int duration = 300;

            var swipePath = new Path();
            swipePath.MoveTo(Start.X, Start.Y);
            swipePath.LineTo(End.X, End.Y);
            
            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));
            
            PerformGesture(gestureBuilder.Build());
        }

        readonly Lazy<Handler> _handler = new Lazy<Handler>(() => new Handler(Looper.MainLooper));

        public void Toast(string Msg)
        {
            _handler.Value.Post(() =>
                Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short).Show());
        }

        public void Click(Location Location)
        {
            const int duration = 1;

            var swipePath = new Path();
            swipePath.MoveTo(Location.X, Location.Y);

            var gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.AddStroke(new GestureDescription.StrokeDescription(swipePath, 0, duration));

            PerformGesture(gestureBuilder.Build());
        }

        class GestureCompletedCallback : AccessibilityService.GestureResultCallback
        {
            readonly ManualResetEventSlim _event;

            public GestureCompletedCallback(ManualResetEventSlim Event)
            {
                _event = Event;
            }

            public override void OnCompleted(GestureDescription GestureDescription)
            {
                _event.Set();
                base.OnCompleted(GestureDescription);
            }

            public override void OnCancelled(GestureDescription GestureDescription)
            {
                _event.Set();
                base.OnCancelled(GestureDescription);
            }
        }

        readonly ManualResetEventSlim _gestureWaitHandle = new ManualResetEventSlim();

        void PerformGesture(GestureDescription Gesture, bool DoWait = true)
        {
            _gestureWaitHandle.Reset();

            _accessibilityService.DispatchGesture(Gesture, new GestureCompletedCallback(_gestureWaitHandle), null);

            if (DoWait)
            {
                AutomataApi.Wait(GestureWait);
            }

            _gestureWaitHandle.Wait();
        }

        const double GestureWait = 0.3;

        public void ContinueClick(Location Location, int Times)
        {
            const int clickTime = 50;
            const int clickDelay = 10;

            while (Times-- > 0)
            {
                var swipePath = new Path();
                swipePath.MoveTo(Location.X, Location.Y);

                var stroke = new GestureDescription.StrokeDescription(swipePath, clickDelay, clickTime);

                var gestureBuilder = new GestureDescription.Builder()
                    .AddStroke(stroke);

                PerformGesture(gestureBuilder.Build(), false);
            }
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
                    NotificationImportance.High)
                {
                    Description = ChannelId
                };

                channel.EnableVibration(true);
                channel.SetVibrationPattern(new []{100L, 200L});

                var notifyManager = (NotificationManager) _accessibilityService.GetSystemService(Context.NotificationService);

                notifyManager.CreateNotificationChannel(channel);
            }

            _channelCreated = true;
        }

        int _notificationId;

        public void MessageBox(string Title, string Message)
        {
            CreateNotificationChannel();

            var builder = new NotificationCompat.Builder(_accessibilityService, ChannelId)
                .SetContentTitle(Title)
                .SetContentText(Message)
                .SetSmallIcon(Android.Resource.Drawable.IcDialogEmail)
                .SetStyle(new NotificationCompat.BigTextStyle()
                    .BigText(Message))
                .SetPriority(NotificationCompat.PriorityHigh)
                .SetAutoCancel(true)
                .SetVibrate(new []{100L, 200L});

            var notifyManager = (NotificationManager)_accessibilityService.GetSystemService(Context.NotificationService);

            notifyManager.Notify(_notificationId++, builder.Build());
        }
    }
}