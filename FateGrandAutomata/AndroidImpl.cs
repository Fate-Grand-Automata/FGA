using System;
using System.Collections.Generic;
using Android.Content;
using Android.Util;
using Android.Views;
using Java.Interop;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        readonly Context _context;

        public AndroidImpl(Context Context)
        {
            _context = Context;
        }

        public (int Width, int Height) WindowSize
        {
            get
            {
                var metrics = new DisplayMetrics();
                var wm = _context.GetSystemService(Context.WindowService).JavaCast<IWindowManager>();

                wm.DefaultDisplay.GetMetrics(metrics);

                return (metrics.WidthPixels, metrics.HeightPixels);
            }
        }

        public void UseSameSnapIn(Action Action)
        {
            throw new NotImplementedException();
        }

        public T UseSameSnapIn<T>(Func<T> Action)
        {
            throw new NotImplementedException();
        }

        public void Scroll(Location Start, Location End)
        {
            throw new NotImplementedException();
        }

        public IEnumerable<Region> FindAll(Pattern Pattern)
        {
            throw new NotImplementedException();
        }

        public IEnumerable<Region> FindAll(Region Region, Pattern Pattern)
        {
            throw new NotImplementedException();
        }

        public void Toast(string Msg)
        {
            throw new NotImplementedException();
        }

        public void Click(Location Location)
        {
            throw new NotImplementedException();
        }

        public void ContinueClick(Location Location, int Times, int Timeout = -1)
        {
            throw new NotImplementedException();
        }

        public void WaitVanish(Region Region, Pattern Image, int? Timeout = null)
        {
            throw new NotImplementedException();
        }

        public bool Exists(Region Region, Pattern Image, int? Timeout = null, double? Similarity = null)
        {
            throw new NotImplementedException();
        }

        public Pattern Save(Region Region)
        {
            throw new NotImplementedException();
        }
    }
}