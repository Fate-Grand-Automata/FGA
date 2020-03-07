using System;
using Android.Content;

namespace FateGrandAutomata
{
    public class FabServiceBroadcastReceiver : BroadcastReceiver
    {
        public const string TOGGLE_SERVICE_INTENT = "toggle_global_fab_service",
            SEND_MEDIA_PROJECTION_TOKEN = "send_media_projection_token",
            MED_PROJ_TOKEN = "med_proj_token";

        public override void OnReceive(Context context, Intent intent)
        {
            switch (intent.Action)
            {
                case TOGGLE_SERVICE_INTENT:
                    ToggleService?.Invoke();
                    break;

                case SEND_MEDIA_PROJECTION_TOKEN:
                    var data = (Intent) intent.GetParcelableExtra(MED_PROJ_TOKEN);
                    MediaProjectionToken?.Invoke(data);
                    break;
            }
        }

        public IntentFilter CreateIntentFilter()
        {
            var intentFilter = new IntentFilter();

            intentFilter.AddAction(TOGGLE_SERVICE_INTENT);
            intentFilter.AddAction(SEND_MEDIA_PROJECTION_TOKEN);

            return intentFilter;
        }

        public event Action ToggleService;

        public event Action<Intent> MediaProjectionToken;
    }
}