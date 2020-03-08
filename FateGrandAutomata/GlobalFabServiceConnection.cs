using Android.Content;
using Android.OS;

namespace FateGrandAutomata
{
    public class GlobalFabServiceConnection : Java.Lang.Object, IServiceConnection, IGlobalFab
    {
        static readonly string TAG = typeof(GlobalFabServiceConnection).FullName;

        readonly MainActivity _mainActivity;

        public GlobalFabServiceConnection(MainActivity Activity)
        {
            _mainActivity = Activity;
        }

        public GlobalFabBinder Binder { get; private set; }

        public void OnServiceConnected(ComponentName name, IBinder service)
        {
            Binder = service as GlobalFabBinder;
            var isConnected = Binder != null;

            if (isConnected)
            {
                _mainActivity.UpdateUiForBoundService();
            }
            else _mainActivity.UpdateUiForUnboundService();
        }

        public void OnServiceDisconnected(ComponentName name)
        {
            Binder = null;
            _mainActivity.UpdateUiForUnboundService();
        }

        public bool HasMediaProjectionToken => Binder?.Service.HasMediaProjectionToken ?? false;

        public bool IsStarted => Binder?.Service.IsStarted ?? false;

        public void Start(Intent MediaProjectionToken = null) => Binder?.Service.Start(MediaProjectionToken);

        public void Stop() => Binder?.Service.Stop();
    }
}