using Android.App;
using Android.OS;
using AndroidX.AppCompat.App;

namespace FateGrandAutomata
{
    [Activity(Label = "Settings")]
    public class SettingsActivity : AppCompatActivity
    {
        protected override void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);

            SetContentView(Resource.Layout.settings);

            SupportFragmentManager
                .BeginTransaction()
                .Replace(Resource.Id.settings_container, new SettingsFragment())
                .Commit();
        }
    }
}