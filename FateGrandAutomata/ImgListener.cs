using Android.Media;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class ImgListener : Java.Lang.Object, ImageReader.IOnImageAvailableListener
    {
        readonly object _syncLock = new object();

        Image _latestImage;
        IPattern _lastestPattern;

        public void OnImageAvailable(ImageReader Reader)
        {
            lock (_syncLock)
            {
                _latestImage?.Close();

                _latestImage = Reader.AcquireLatestImage();
                _lastestPattern = null;
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

                return _lastestPattern ??= new DroidCvPattern(_latestImage);
            }
        }
    }
}