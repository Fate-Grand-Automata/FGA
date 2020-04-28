using System.IO;
using Android.Graphics;
using Android.Media;
using Android.OS;
using CoreAutomata;
using Java.Lang;
using Org.Opencv.Core;
using Org.Opencv.Imgproc;
using Encoding = System.Text.Encoding;
using Path = System.IO.Path;
using Process = Java.Lang.Process;

namespace FateGrandAutomata
{
    public class ImgListener : Java.Lang.Object, ImageReader.IOnImageAvailableListener
    {
        readonly object _syncLock = new object();

        IPattern _lastestPattern;
        bool _newImgAvailable;
        ImageReader _imageReader;

        Mat _convertedMat,
            _colorCorrectedMat;

        Bitmap _readBitmap;
        bool _cropRequired;

        public ImgListener(ImageReader ImageReader)
        {
            _imageReader = ImageReader;
            _convertedMat = new Mat();
            _colorCorrectedMat = new Mat();
        }

        protected override void Dispose(bool Disposing)
        {
            if (Disposing)
            {
                _convertedMat.Release();
                _colorCorrectedMat.Release();

                _convertedMat = _colorCorrectedMat = null;

                _lastestPattern?.Dispose();
                _lastestPattern = null;

                _readBitmap?.Recycle();
                _readBitmap = null;

                _imageReader = null;
            }

            base.Dispose(Disposing);
        }

        public void OnImageAvailable(ImageReader Reader)
        {
            lock (_syncLock)
            {
                _newImgAvailable = true;
            }
        }

        Process _superUser;
        StreamWriter _superUserStreamWriter;
        byte[] _buffer;
        Mat _rootLoadMat, _rootConvertMat = new Mat();

        IPattern AcquirePatternRoot()
        {
            if (_superUser == null)
            {
                _superUser = Runtime.GetRuntime().Exec("su", null, null);;
                _superUserStreamWriter = new StreamWriter(_superUser.OutputStream, Encoding.ASCII);
            }

            var imgPath = Path.Combine(AutomataApi.StorageDir, "sshot.raw");

            _superUserStreamWriter.WriteLine($"/system/bin/screencap {imgPath}");
            _superUserStreamWriter.Flush();

            // Wait
            // https://stackoverflow.com/a/16160785/5377194
            {
                _superUserStreamWriter.WriteLine("echo -n 0");
                _superUserStreamWriter.Flush();

                _superUser.InputStream.ReadByte();
            }

            using var f = File.OpenRead(imgPath);
            using var reader = new BinaryReader(f, Encoding.ASCII);
            var w = reader.ReadInt32();
            var h = reader.ReadInt32();
            var format = reader.ReadInt32();

            if (Build.VERSION.SdkInt >= BuildVersionCodes.O)
            {
                reader.ReadInt32();
            }

            if (_buffer == null)
            {
                // If format is not RGBA, notify
                if (format != 1)
                {
                    AutomataApi.Toast($"Unexpected raw image format: {format}");
                }

                _buffer = new byte[w * h * 4];
                _rootLoadMat = new Mat(h, w, CvType.Cv8uc4);
            }

            reader.Read(_buffer, 0, _buffer.Length);

            _rootLoadMat.Put(0, 0, _buffer);

            Imgproc.CvtColor(_rootLoadMat, _rootConvertMat, Imgproc.ColorRgba2gray);

            return new DroidCvPattern(_rootConvertMat, false);
        }

        public IPattern AcquirePattern()
        {
            return AcquirePatternRoot();

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