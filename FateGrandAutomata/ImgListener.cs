using Android.Media;
using CoreAutomata;
using Org.Opencv.Core;
using Org.Opencv.Imgcodecs;

namespace FateGrandAutomata
{
    public class ImgListener : Java.Lang.Object, ImageReader.IOnImageAvailableListener
    {
        readonly object _syncLock = new object();

        Image _latestImage;

        public void OnImageAvailable(ImageReader Reader)
        {
            lock (_syncLock)
            {
                _latestImage?.Close();

                _latestImage = Reader.AcquireLatestImage();
            }
        }

        public IPattern AcquirePattern()
        {
            lock (_syncLock)
            {
                if (_latestImage == null)
                {
                    return null;
                }

                var byteBuffer = _latestImage.GetPlanes()[0].Buffer;
                var data = new byte[byteBuffer.Remaining()];
                byteBuffer.Get(data);

                var mat = Imgcodecs.Imdecode(new MatOfByte(data), Imgcodecs.CvLoadImageUnchanged);

                _latestImage.Close();
                _latestImage = null;

                return new DroidCvPattern(mat);
            }
        }
    }
}