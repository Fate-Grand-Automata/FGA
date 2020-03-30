using System.Collections.Generic;
using System.IO;
using Android.Graphics;
using Android.Media;
using CoreAutomata;
using Org.Opencv.Core;
using Org.Opencv.Imgcodecs;
using Org.Opencv.Imgproc;
using Rect = Org.Opencv.Core.Rect;
using Region = CoreAutomata.Region;
using Size = CoreAutomata.Size;
using Stream = System.IO.Stream;

namespace FateGrandAutomata
{
    public class DroidCvPattern : IPattern
    {
        DroidCvPattern(Mat Mat)
        {
            this.Mat = Mat;
        }

        public DroidCvPattern(Image Image) : this(MatFromImage(Image))
        {
        }

        static Mat MatFromImage(Image Image)
        {
            var width = Image.Width;
            var height = Image.Height;

            var planes = Image.GetPlanes();
            var buffer = planes[0].Buffer;

            var pixelStride = planes[0].PixelStride;
            var rowStride = planes[0].RowStride;
            var rowPadding = rowStride - pixelStride * width;

            var bitmap = Bitmap.CreateBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.Argb8888);
            bitmap.CopyPixelsFromBuffer(buffer);
            bitmap = Bitmap.CreateBitmap(bitmap, 0, 0, width, height);

            using var mat = new Mat();
            Org.Opencv.Android.Utils.BitmapToMat(bitmap, mat);

            var cvtMat = new Mat();

            Imgproc.CvtColor(mat, cvtMat, Imgproc.ColorRgba2bgr);

            return cvtMat;
        }

        public DroidCvPattern(Stream Stream)
        {
            var buffer = new byte[Stream.Length];
            using var ms = new MemoryStream(buffer);
            Stream.CopyTo(ms);

            using var raw = new MatOfByte(buffer);

            Mat = Imgcodecs.Imdecode(raw, Imgcodecs.CvLoadImageColor);
        }

        public Mat Mat { get; }

        public IPattern Resize(Size Size)
        {
            var result = new Mat();

            Imgproc.Resize(Mat, result, new Org.Opencv.Core.Size(Size.Width, Size.Height));

            return new DroidCvPattern(result);
        }

        public IPattern Crop(Region Region)
        {
            var rect = new Rect(Region.X, Region.Y, Region.W, Region.H);

            if (rect.X + rect.Width > Width)
            {
                rect.X = Width - rect.Width;
            }

            if (rect.Y + rect.Height > Height)
            {
                rect.Y = Height - rect.Height;
            }

            var result = new Mat(Mat, rect);

            return new DroidCvPattern(result);
        }

        public void Save(string Filename)
        {
            Imgcodecs.Imwrite(Filename, Mat);
        }

        public bool IsMatch(IPattern Template, double Similarity)
        {
            using var result = new Mat();

            Imgproc.MatchTemplate(Mat, (Template as DroidCvPattern)?.Mat, result, Imgproc.TmCcoeffNormed);

            using var minMaxLocResult = Core.MinMaxLoc(result);

            AutomataApi.WriteDebug($"Similarity: {minMaxLocResult.MaxVal} >= {Similarity}");

            return minMaxLocResult.MaxVal >= Similarity;
        }

        public int Width => Mat.Width();

        public int Height => Mat.Height();

        public IEnumerable<Match> FindMatches(IPattern Template, double Similarity)
        {
            using var result = new Mat();

            // max is used for tmccoeff
            Imgproc.MatchTemplate(Mat, (Template as DroidCvPattern)?.Mat, result, Imgproc.TmCcoeffNormed);

            Imgproc.Threshold(result, result, 0.1, 1, Imgproc.ThreshTozero);

            while (true)
            {
                using var minMaxLocResult = Core.MinMaxLoc(result);
                var score = minMaxLocResult.MaxVal;

                if (score >= Similarity)
                {
                    var loc = minMaxLocResult.MaxLoc;
                    var location = new Region((int)loc.X, (int)loc.Y, Template.Width, Template.Height);
                    yield return new Match(location, score);

                    Imgproc.FloodFill(result, null, loc, new Scalar(0));
                }
                else break;
            }
        }
    }
}