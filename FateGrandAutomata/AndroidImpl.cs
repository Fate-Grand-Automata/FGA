using System;
using System.IO;
using System.Threading.Tasks;
using Android.Content;
using Android.OS;
using Android.Widget;
using AndroidX.Preference;
using CoreAutomata;
using Org.Opencv.Android;
using Environment = Android.OS.Environment;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        readonly ScriptRunnerService _accessibilityService;
        readonly ISharedPreferences _prefs;

        public AndroidImpl(ScriptRunnerService AccessibilityService)
        {
            _accessibilityService = AccessibilityService;
            _prefs = PreferenceManager.GetDefaultSharedPreferences(_accessibilityService);

            RegisterStorageRootDir();

            OpenCVLoader.InitDebug();
        }

        public bool DebugMode => _prefs.GetBoolean(_accessibilityService.GetString(Resource.String.pref_debug_mode), false);

        public Region WindowRegion => CutoutManager.GetCutoutAppliedRegion(_accessibilityService);

        readonly Lazy<Handler> _handler = new Lazy<Handler>(() => new Handler(Looper.MainLooper));

        public void Toast(string Msg)
        {
            _handler.Value.Post(() =>
                Android.Widget.Toast.MakeText(_accessibilityService, Msg, ToastLength.Short).Show());
        }

        public IPattern LoadPattern(Stream Stream)
        {
            return new DroidCvPattern(Stream);
        }

        public IPattern GetResizableBlankPattern()
        {
            return new DroidCvPattern();
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

        public void Highlight(Region Region, double Timeout)
        {
            HighlightView.AddRegion(Region);

            Task.Delay(TimeSpan.FromSeconds(Timeout))
                .ContinueWith(M => HighlightView.RemoveRegion(Region));
        }

        public static void RegisterStorageRootDir()
        {
            AutomataApi.SetStorageRootDir(Environment.ExternalStorageDirectory.AbsolutePath);
        }
    }
}