using System;
using System.IO;
using Android.OS;
using Android.Widget;
using CoreAutomata;
using Org.Opencv.Android;
using Environment = Android.OS.Environment;

namespace FateGrandAutomata
{
    public partial class AndroidImpl : IPlatformImpl
    {
        readonly ScriptRunnerService _accessibilityService;

        public AndroidImpl(ScriptRunnerService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;

            RegisterStorageRootDir();

            OpenCVLoader.InitDebug();
        }

        public Region WindowRegion => CutoutManager.GetCutoutAppliedRegion(_accessibilityService);

        readonly Lazy<Handler> _handler = new Lazy<Handler>(() => new Handler(Looper.MainLooper));

        public void Toast(string Msg)
        {
            _handler.Value.Post(() =>
                Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short).Show());
        }

        public IPattern Screenshot()
        {
            return ScriptRunnerService
                .Instance
                .AcquireLatestImage();
        }

        public IPattern LoadPattern(Stream Stream)
        {
            return new DroidCvPattern(Stream);
        }

        public void MessageBox(string Title, string Message)
        {
            _handler.Value.Post(() =>
            {
                var msg = $"{Title.ToUpper()}: {Message}";

                Android.Widget.Toast
                    .MakeText(_accessibilityService, msg, ToastLength.Long)
                    .Show();

                _accessibilityService.ShowStatusNotification(msg);
            });
        }

        public static void RegisterStorageRootDir()
        {
            AutomataApi.SetStorageRootDir(Environment.ExternalStorageDirectory.AbsolutePath);
        }
    }
}