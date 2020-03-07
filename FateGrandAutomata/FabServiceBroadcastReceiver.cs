using System;
using Android.Content;

namespace FateGrandAutomata
{
    public class FabServiceBroadcastReceiver : BroadcastReceiver
    {
        public const string TOGGLE_SERVICE_INTENT = "toggle_global_fab_service";

        public override void OnReceive(Context context, Intent intent)
        {
            switch (intent.Action)
            {
                case TOGGLE_SERVICE_INTENT:
                    ToggleService?.Invoke();
                    break;
            }
        }

        public IntentFilter CreateIntentFilter()
        {
            var intentFilter = new IntentFilter();

            intentFilter.AddAction(TOGGLE_SERVICE_INTENT);

            return intentFilter;
        }

        public event Action ToggleService;
    }
}