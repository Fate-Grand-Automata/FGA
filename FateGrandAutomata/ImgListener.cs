using Android.Media;
using CoreAutomata;
using Org.Opencv.Core;

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

                var mat = new Mat(_latestImage.Width, _latestImage.Height, CvType.Cv8uc4);

                mat.Put(0, 0, data);

                return new DroidCvPattern(mat);
            }
        }
    }
}