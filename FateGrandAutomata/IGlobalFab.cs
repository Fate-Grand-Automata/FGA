using Android.Content;

namespace FateGrandAutomata
{
    public interface IGlobalFab
    {
        bool HasMediaProjectionToken { get; }

        bool IsStarted { get; }

        void Start(Intent MediaProjectionToken = null);

        void Stop();
    }
}