using System;
using Org.Opencv.Core;

namespace FateGrandAutomata
{
    class DisposableMat : IDisposable
    {
        public Mat Mat { get; private set; }

        public DisposableMat() : this(new Mat()) { }
        
        public DisposableMat(Mat Mat)
        {
            this.Mat = Mat;
        }

        public void Dispose()
        {
            Mat.Release();
            Mat = null;
        }
    }
}