using Android.Graphics;
using Android.Media;
using CoreAutomata;
using Org.Opencv.Core;
using Org.Opencv.Imgproc;

namespace FateGrandAutomata
{
    public class ImgListener : Java.Lang.Object, ImageReader.IOnImageAvailableListener
    {
        readonly object _syncLock = new object();

        IPattern _lastestPattern;
        bool _newImgAvailable;
        readonly ImageReader _imageReader;

        public ImgListener(ImageReader ImageReader)
        {
            _imageReader = ImageReader;
        }

        public void OnImageAvailable(ImageReader Reader)
        {
            lock (_syncLock)
            {
                _newImgAvailable = true;
            }
        }

        public IPattern AcquirePattern()
        {
            var createNewPattern = false;

            lock (_syncLock)
            {
                if (_newImgAvailable)
                {
                    createNewPattern = true;
                    _newImgAvailable = false;
                }
            }

            if (createNewPattern)
            {
                var latestImage = _imageReader.AcquireLatestImage();

                if (latestImage != null)
                {
                    try
                    {
                        _lastestPattern?.Dispose();
                        _lastestPattern = MatFromImage(latestImage);
                    }
                    finally
                    {
                        // Close is required for ImageReader. Dispose doesn't work.
                        latestImage.Close();
                    }
                }
            }

            return _lastestPattern;
        }

        readonly Mat _convertedMat = new Mat(),
            _colorCorrectedMat = new Mat();

        Bitmap _readBitmap;
        bool _cropRequired;

        IPattern MatFromImage(Image Image)
        {
            var width = Image.Width;
            var height = Image.Height;

            var planes = Image.GetPlanes();
            var buffer = planes[0].Buffer;

            if (_readBitmap == null)
            {
                var pixelStride = planes[0].PixelStride;
                var rowStride = planes[0].RowStride;
                var rowPadding = rowStride - pixelStride * width;

                _cropRequired = (rowPadding / pixelStride) != 0;

                _readBitmap = Bitmap.CreateBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.Argb8888);
            }

            _readBitmap.CopyPixelsFromBuffer(buffer);

            if (_cropRequired)
            {
                var correctedBitmap = Bitmap.CreateBitmap(_readBitmap, 0, 0, width, height);
                Org.Opencv.Android.Utils.BitmapToMat(correctedBitmap, _convertedMat);
                // if a new Bitmap was created, we need to tell the Garbage Collector to delete it immediately
                correctedBitmap.Recycle();
            }
            else Org.Opencv.Android.Utils.BitmapToMat(_readBitmap, _convertedMat);

            Imgproc.CvtColor(_convertedMat, _colorCorrectedMat, Imgproc.ColorRgba2gray);

            return new DroidCvPattern(_colorCorrectedMat, false);
        }
    }
}