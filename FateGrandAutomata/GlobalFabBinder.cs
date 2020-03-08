using Android.OS;

namespace FateGrandAutomata
{
    public class GlobalFabBinder : Binder
    {
        public GlobalFabService Service { get; }

        public GlobalFabBinder(GlobalFabService Service)
        {
            this.Service = Service;
        }
    }
}