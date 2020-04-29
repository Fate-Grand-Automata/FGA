using System;
using System.Linq;
using Android;
using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.OS;
using Android.Provider;
using Android.Runtime;
using Android.Widget;
using AndroidX.AppCompat.App;
using AndroidX.Core.App;
using AndroidX.Core.Content;
using AndroidX.Preference;
using AlertDialog = Android.App.AlertDialog;

namespace FateGrandAutomata
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity, PreferenceFragmentCompat.IOnPreferenceStartFragmentCallback
    {
        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);
            Xamarin.Essentials.Platform.Init(this, SavedInstanceState);
            SetContentView(Resource.Layout.activity_main);

            var toolbar = FindViewById<AndroidX.AppCompat.Widget.Toolbar>(Resource.Id.toolbar);
            SetSupportActionBar(toolbar);

            var serviceToggleBtn = FindViewById<Button>(Resource.Id.service_toggle_btn);
            serviceToggleBtn.Click += ServiceToggleBtnOnClick;

            // Only once
            if (SavedInstanceState == null)
            {
                SupportFragmentManager
                    .BeginTransaction()
                    .Replace(Resource.Id.main_pref_frame, new MainSettingsFragment())
                    .Commit();
                CheckPermissions();
                IgnoreBatteryOptimizations();
            }

            AndroidImpl.RegisterStorageRootDir();
        }

        public override void OnAttachedToWindow()
        {
            base.OnAttachedToWindow();

            CutoutManager.ApplyCutout(this);
        }

        void IgnoreBatteryOptimizations()
        {
            var powerManager = (PowerManager) GetSystemService(PowerService);

            if (powerManager.IsIgnoringBatteryOptimizations(PackageName))
            {
                return;
            }

            StartActivity(new Intent(Settings.ActionRequestIgnoreBatteryOptimizations,
                Android.Net.Uri.Parse("package:" + PackageName)));
        }

        protected override void OnActivityResult(int RequestCode, Result ResultCode, Intent Data)
        {
            if (RequestCode == RequestMediaProjection)
            {
                if (ResultCode != Result.Ok)
                {
                    Toast.MakeText(this, "Canceled", ToastLength.Short).Show();
                    return;
                }

                ScriptRunnerService.Instance.Start(Data);
            }
        }

        void CheckPermissions()
        {
            var permissionsToCheck = new[]
            {
                Manifest.Permission.WriteExternalStorage,
                Manifest.Permission.ReadExternalStorage
            };

            var permissionsToRequest = permissionsToCheck
                .Where(M => ContextCompat.CheckSelfPermission(this, M) != Permission.Granted)
                .ToArray();

            if (permissionsToRequest.Length > 0)
            {
                ActivityCompat.RequestPermissions(
                    this,
                    permissionsToRequest,
                    0);
            }
        }

        bool CheckAccessibilityService()
        {
            if (ScriptRunnerService.Instance != null)
                return true;

            new AlertDialog.Builder(this)
                .SetTitle("Accessibility Disabled")
                .SetMessage("Turn on accessibility for this app from System settings. If it is already On, turn it OFF and start again.")
                .SetPositiveButton("Go To Settings", (S, E) =>
                {
                    // Open Acessibility Settings
                    var intent = new Intent(Settings.ActionAccessibilitySettings);
                    StartActivity(intent);
                })
                .SetNegativeButton("Cancel", (S, E) => { })
                .Show();

            return false;
        }

        void ServiceToggleBtnOnClick(object Sender, EventArgs EventArgs)
        {
            if (!CheckAccessibilityService()) 
                return;

            var instance = ScriptRunnerService.Instance;

            if (instance.ServiceStarted)
            {
                instance.Stop();
            }
            else
            {
                if (ScriptRunnerService.Instance.WantsMediaProjectionToken)
                {
                    // This initiates a prompt dialog for the user to confirm screen projection.
                    StartActivityForResult(instance.MediaProjectionManager.CreateScreenCaptureIntent(), RequestMediaProjection);
                }
                else instance.Start();
            }
        }

        public override void OnRequestPermissionsResult(int RequestCode, string[] Permissions, [GeneratedEnum] Android.Content.PM.Permission[] GrantResults)
        {
            Xamarin.Essentials.Platform.OnRequestPermissionsResult(RequestCode, Permissions, GrantResults);

            base.OnRequestPermissionsResult(RequestCode, Permissions, GrantResults);
        }

        const int RequestMediaProjection = 1;

        public bool OnPreferenceStartFragment(PreferenceFragmentCompat Caller, Preference Pref)
        {
            var args = Pref.Extras;
            var fragment = SupportFragmentManager
                .FragmentFactory
                .Instantiate(ClassLoader, Pref.Fragment);
            fragment.Arguments = args;
            fragment.SetTargetFragment(Caller, 0);

            SupportFragmentManager
                .BeginTransaction()
                .SetCustomAnimations(Android.Resource.Animation.FadeIn,
                    Android.Resource.Animation.FadeOut,
                    Android.Resource.Animation.FadeIn,
                    Android.Resource.Animation.FadeOut)
                .Replace(Resource.Id.main_pref_frame, fragment)
                .AddToBackStack(null)
                .Commit();

            return true;
        }
    }
}

