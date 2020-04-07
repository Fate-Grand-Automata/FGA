using Android.App;
using Android.OS;
using AndroidX.AppCompat.App;

namespace FateGrandAutomata
{
    [Activity(Label = "Edit AutoSkill Item")]
    public class AutoSkillItemActivity: AppCompatActivity
    {
        public const string AutoSkillItemKey = nameof(AutoSkillItemKey);

        protected override void OnCreate(Bundle SavedInstanceState)
        {
            base.OnCreate(SavedInstanceState);

            SetContentView(Resource.Layout.settings);

            // Add the fragment only on first launch
            if (SavedInstanceState == null)
            {
                var fragment = new AutoSkillSettingsFragment();
                var args = new Bundle();
                args.PutString(AutoSkillItemKey, Intent.GetStringExtra(AutoSkillItemKey));

                fragment.Arguments = args;

                SupportFragmentManager
                    .BeginTransaction()
                    .Replace(Resource.Id.settings_container, fragment)
                    .Commit();
            }
        }
    }
}