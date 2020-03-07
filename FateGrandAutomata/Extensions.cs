using System;
using System.Linq;
using Android.Content;
using Android.Provider;

namespace FateGrandAutomata
{
    public static class Extensions
    {
        public static bool IsAccessibilityServiceEnabled<T>(this Context Context)
        {
            var expectedComponentName = new ComponentName(Context, Java.Lang.Class.FromType(typeof(T)));

            var enabledServicesSetting = Settings.Secure.GetString(Context.ContentResolver, Settings.Secure.EnabledAccessibilityServices);
            if (enabledServicesSetting == null)
                return false;

            return enabledServicesSetting.Split(":", StringSplitOptions.RemoveEmptyEntries)
                .Select(ComponentName.UnflattenFromString)
                .Any(M => M.Equals(expectedComponentName));
        }
    }
}