using Android.Graphics;
using Android.Hardware.Display;
using Android.Media;
using Android.Media.Projection;
using Android.Util;
using Android.Views;
using CoreAutomata;
using Org.Opencv.Core;
using Org.Opencv.Imgproc;

namespace FateGrandAutomata
{
    public class MediaProjectionScreenshotService : IScreenshotService
    {
        MediaProjection _mediaProjection;
        VirtualDisplay _virtualDisplay;
        ImageReader _imageReader;

        IPattern _lastestPattern;

        Mat _convertedMat,
            _colorCorrectedMat;

        Bitmap _readBitmap;
        bool _cropRequired;

        public MediaProjectionScreenshotService(MediaProjection MediaProjection, DisplayMetrics Metrics)
        {
            _mediaProjection = MediaProjection;

            var screenDensity = (int)Metrics.DensityDpi;
            var screenWidth = Metrics.WidthPixels;
            var screenHeight = Metrics.HeightPixels;

            _convertedMat = new Mat();
            _colorCorrectedMat = new Mat();

            _imageReader = ImageReader.NewInstance(screenWidth, screenHeight, (ImageFormatType)1, 2);

            _virtualDisplay = _mediaProjection.CreateVirtualDisplay("ScreenCapture",
                screenWidth, screenHeight, screenDensity,
                DisplayFlags.None, _imageReader.Surface, null, null);
        }

        public IPattern TakeScreenshot()
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

            return _lastestPattern;
        }

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

        public void Dispose()
        {
            _convertedMat.Release();
            _colorCorrectedMat.Release();

            _convertedMat = _colorCorrectedMat = null;

            _lastestPattern?.Dispose();
            _lastestPattern = null;

            _readBitmap?.Recycle();
            _readBitmap = null;

            _virtualDisplay?.Release();
            _virtualDisplay = null;

            _imageReader?.Close();
            _imageReader = null;

            _mediaProjection?.Stop();
            _mediaProjection = null;
        }
    }
}