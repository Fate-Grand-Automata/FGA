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

            var settingsBtn = FindViewById<Button>(Resource.Id.configure_btn);
            settingsBtn.Click += (S, E) => OpenSettings();

            CheckPermissions();
            IgnoreBatteryOptimizations();
            ShowStatusText();
        }

        public override void OnAttachedToWindow()
        {
            base.OnAttachedToWindow();

            CutoutManager.ApplyCutout(this);
        }

        protected override void OnRestart()
        {
            base.OnRestart();
            ShowStatusText();
        }

        void ShowStatusText()
        {
            if (ScriptRunnerService.Instance == null)
            {
                return;
            }

            var statusTextView = FindViewById<TextView>(Resource.Id.status_textview);

            var autoskillOn = Preferences.Instance.EnableAutoSkill;
            var autoskillCmd = autoskillOn
                ? $" - {Preferences.Instance.SkillCommand}"
                : "";

            var refillPrefs = Preferences.Instance.Refill;

            var autoRefillOn = refillPrefs.Enabled;
            var autoRefillStatus = autoRefillOn
                ? $" - {refillPrefs.Enabled} x{refillPrefs.Repetitions}"
                : "";

            var supportPrefs = Preferences.Instance.Support;
            var preferredMode = supportPrefs.SelectionMode == SupportSelectionMode.Preferred;

            static string Any(string Value) => string.IsNullOrWhiteSpace(Value)
                ? "Any"
                : Value;

            var supportStatus = preferredMode
                ? $"Servants: '{Any(supportPrefs.PreferredServants)}', CEs: '{Any(supportPrefs.PreferredCEs)}'"
                : "";

            static string OnOff(bool Value) => Value ? "ON" : "OFF";

            var statusText = $"Mode: {Preferences.Instance.ScriptMode}";

            if (Preferences.Instance.ScriptMode == ScriptMode.Battle)
            {
                statusText += $@"
Server: {Preferences.Instance.GameServer}
Auto Skill: {OnOff(autoskillOn)}{autoskillCmd}
Auto Refill: {OnOff(autoRefillOn)}{autoRefillStatus}
Auto Support Selection: {supportPrefs.SelectionMode}
{supportStatus}
";
            }

            statusTextView.Text = statusText;
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

        void OpenSettings()
        {
            StartActivity(typeof(SettingsActivity));
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
                if (ScriptRunnerService.Instance.HasMediaProjectionToken)
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

