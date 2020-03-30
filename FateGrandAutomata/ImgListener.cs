using Android.Media;
using CoreAutomata;

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

                return new DroidCvPattern(_latestImage);
            }
        }
    }
}