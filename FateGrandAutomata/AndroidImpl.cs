using System;
using System.Collections.Generic;

namespace FateGrandAutomata
{
    public class AndroidImpl : IPlatformImpl
    {
        public void UseSameSnapIn(Action Action)
        {
            throw new NotImplementedException();
        }

        public T UseSameSnapIn<T>(Func<T> Action)
        {
            throw new NotImplementedException();
        }

        public void Scroll(Point Start, Point End)
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

        public void SetImmersiveMode(bool Active)
        {
            throw new NotImplementedException();
        }

        public void AutoGameArea(bool Active)
        {
            throw new NotImplementedException();
        }

        public Region GetGameArea()
        {
            throw new NotImplementedException();
        }

        public void SetGameArea(Region Region)
        {
            throw new NotImplementedException();
        }

        public void SetScriptDimension(bool CompareByWidth, int Pixels)
        {
            throw new NotImplementedException();
        }

        public void SetCompareDimension(bool CompareByWidth, int Pixels)
        {
            throw new NotImplementedException();
        }

        public void Toast(string Msg)
        {
            throw new NotImplementedException();
        }
    }
}