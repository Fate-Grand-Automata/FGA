using System;
using Android.Media;
using Android.Views;
using CoreAutomata;

namespace FateGrandAutomata
{
    public class ImgListener : Java.Lang.Object, ImageReader.IOnImageAvailableListener
    {
        readonly object _syncLock = new object();

        IPattern _lastestPattern;
        bool _newImgAvailable;
        readonly ImageReader _imageReader;
        readonly Display _display;
        readonly bool _initialRotated;

        static bool IsRotated(SurfaceOrientation Orientation)
        {
            return Orientation switch
            {
                SurfaceOrientation.Rotation90 => true,
                SurfaceOrientation.Rotation270 => true,
                _ => false
            };
        }

        public ImgListener(ImageReader ImageReader, Display Display)
        {
            _imageReader = ImageReader;
            _display = Display;
            _initialRotated = IsRotated(Display.Rotation);
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
            lock (_syncLock)
            {
                if (_newImgAvailable)
                {
                    var latestImage = _imageReader.AcquireLatestImage();

                    try
                    {
                        _lastestPattern?.Dispose();
                        _lastestPattern = new DroidCvPattern(latestImage);

                        var currentRotated = IsRotated(_display.Rotation);

                        if (currentRotated != _initialRotated)
                        {
                            _lastestPattern = CorrectRotation(_lastestPattern);
                        }
                    }
                    finally
                    {
                        // Close is required for ImageReader. Dispose doesn't work.
                        latestImage.Close();
                    }

                    _newImgAvailable = false;
                }

                return _lastestPattern;
            }
        }

        static IPattern CorrectRotation(IPattern Pattern)
        {
            var w = Pattern.Width;
            var h = Pattern.Height;

            // Portrait in Landscape
            if (w > h)
            {
                var scaleDown = h / (double) w;
                var newWidth = h * scaleDown;

                var cropRect = new Region((int) Math.Round((w - newWidth) / 2), 0, (int) Math.Round(newWidth), h);
                return Pattern
                    .Crop(cropRect)
                    .Resize(new Size(h, w));
            }
            else // Landscape in Portrait
            {
                var scaleDown = w / (double) h;
                var newHeight = w * scaleDown;

                var cropRect = new Region(0, (int) Math.Round((h - newHeight) / 2), w, (int) Math.Round(newHeight));
                return Pattern
                    .Crop(cropRect)
                    .Resize(new Size(h, w));
            }
        }
    }
}