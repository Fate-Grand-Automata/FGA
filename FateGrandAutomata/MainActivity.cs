using System;
using Android.App;
using Android.Content;
using Android.Media.Projection;
using Android.OS;
using Android.Provider;
using Android.Runtime;
using Android.Support.Design.Widget;
using Android.Support.V7.App;
using Android.Views;
using Android.Widget;
using CoreAutomata;
using AlertDialog = Android.App.AlertDialog;

namespace FateGrandAutomata
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme.NoActionBar", MainLauncher = true)]
    public class MainActivity : AppCompatActivity
    {
        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            Xamarin.Essentials.Platform.Init(this, savedInstanceState);
            SetContentView(Resource.Layout.activity_main);

            var toolbar = FindViewById<Android.Support.V7.Widget.Toolbar>(Resource.Id.toolbar);
            SetSupportActionBar(toolbar);

            var fab = FindViewById<FloatingActionButton>(Resource.Id.fab);
            fab.Click += FabOnClick;

            var debugMsgTextBox = FindViewById<TextView>(Resource.Id.debug_msg);
            AutomataApi.DebugMsgReceived += Msg =>
            {
                debugMsgTextBox.Append("\n");
                debugMsgTextBox.Append(Msg);
            };
            
            _mediaProjectionManager = (MediaProjectionManager) GetSystemService(MediaProjectionService);
        }

        protected override void OnActivityResult(int requestCode, Result resultCode, Intent data)
        {
            if (requestCode == REQUEST_MEDIA_PROJECTION)
            {
                if (resultCode != Result.Ok)
                {
                    Toast.MakeText(this, "Canceled", ToastLength.Short).Show();
                    return;
                }

                GlobalFabService.Instance.Start(data);
            }
        }

        public override bool OnCreateOptionsMenu(IMenu menu)
        {
            MenuInflater.Inflate(Resource.Menu.menu_main, menu);
            return true;
        }

        public override bool OnOptionsItemSelected(IMenuItem item)
        {
            var id = item.ItemId;
            if (id == Resource.Id.action_settings)
            {
                return true;
            }

            return base.OnOptionsItemSelected(item);
        }

        void FabOnClick(object sender, EventArgs eventArgs)
        {
            if (GlobalFabService.Instance != null)
            {
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
                    else StartActivityForResult(_mediaProjectionManager.CreateScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                }
            }
            else
            {
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
            }
        }
        public override void OnRequestPermissionsResult(int requestCode, string[] permissions, [GeneratedEnum] Android.Content.PM.Permission[] grantResults)
        {
            Xamarin.Essentials.Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);

            base.OnRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        const int REQUEST_MEDIA_PROJECTION = 1;

        MediaProjectionManager _mediaProjectionManager;
    }
}

