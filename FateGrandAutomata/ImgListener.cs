using Android.Graphics;
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

                var width = _latestImage.Width;
                var height = _latestImage.Height;

                var planes = _latestImage.GetPlanes();
                var buffer = planes[0].Buffer;

                var pixelStride = planes[0].PixelStride;
                var rowStride = planes[0].RowStride;
                var rowPadding = rowStride - pixelStride * width;

                var bitmap = Bitmap.CreateBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.Argb8888);
                bitmap.CopyPixelsFromBuffer(buffer);
                bitmap = Bitmap.CreateBitmap(bitmap, 0, 0, width, height);
                
                var mat = new Mat();
                Org.Opencv.Android.Utils.BitmapToMat(bitmap, mat);

                return new DroidCvPattern(mat);
            }
        }
    }
}