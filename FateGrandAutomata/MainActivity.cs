using System;
using Android;
using Android.App;
using Android.Content;
using Android.Content.PM;
using Android.OS;
using Android.Provider;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using AndroidX.AppCompat.App;
using AndroidX.Core.App;
using AndroidX.Core.Content;
using AlertDialog = Android.App.AlertDialog;

namespace FateGrandAutomata
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity
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

            CheckStorageWritePermission();
            IgnoreBatteryOptimizations();
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

                GlobalFabService.Instance.Start(Data);
            }
        }

        public override bool OnCreateOptionsMenu(IMenu Menu)
        {
            MenuInflater.Inflate(Resource.Menu.menu_main, Menu);
            return true;
        }

        public override bool OnOptionsItemSelected(IMenuItem Item)
        {
            var id = Item.ItemId;
            if (id == Resource.Id.action_settings)
            {
                OpenSettings();
                return true;
            }

            return base.OnOptionsItemSelected(Item);
        }

        void OpenSettings()
        {
            StartActivity(typeof(SettingsActivity));
        }

        void CheckStorageWritePermission()
        {
            if (ContextCompat.CheckSelfPermission(this, Manifest.Permission.WriteExternalStorage) != Permission.Granted)
            {
                ActivityCompat.RequestPermissions(
                    this,
                    new[] { Manifest.Permission.WriteExternalStorage },
                    0);
            }
        }

        bool CheckAccessibilityService()
        {
            if (GlobalFabService.Instance != null)
                return true;

            var alertDialog = new AlertDialog.Builder(this);
            alertDialog.SetTitle("Accessibility Disabled")
                .SetMessage("Turn on accessibility for this app from System settings")
                .SetPositiveButton("Go To Settings", (S, E) =>
                {
                    if (S is Dialog dialog)
                    {
                        dialog.Dismiss();
                    }

                    // Open Acessibility Settings
                    var intent = new Intent(Settings.ActionAccessibilitySettings);
                    StartActivity(intent);
                })
                .SetNegativeButton("Cancel", (S, E) =>
                {
                    if (S is Dialog dialog)
                    {
                        dialog.Dismiss();
                    }
                })
                .Show();

            return false;
        }

        void ServiceToggleBtnOnClick(object Sender, EventArgs EventArgs)
        {
            if (!CheckAccessibilityService()) 
                return;

            var instance = GlobalFabService.Instance;

            if (instance.ServiceStarted)
            {
                instance.Stop();
            }
            else
            {
                if (GlobalFabService.Instance.HasMediaProjectionToken)
                {
                    instance.Start();
                }
                // This initiates a prompt dialog for the user to confirm screen projection.
                else StartActivityForResult(instance.MediaProjectionManager.CreateScreenCaptureIntent(), RequestMediaProjection);
            }
        }
        public override void OnRequestPermissionsResult(int RequestCode, string[] Permissions, [GeneratedEnum] Android.Content.PM.Permission[] GrantResults)
        {
            Xamarin.Essentials.Platform.OnRequestPermissionsResult(RequestCode, Permissions, GrantResults);

            base.OnRequestPermissionsResult(RequestCode, Permissions, GrantResults);
        }

        const int RequestMediaProjection = 1;
    }
}

