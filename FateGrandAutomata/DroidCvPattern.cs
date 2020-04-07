using System.Collections.Generic;
using System.IO;
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
        readonly bool _ownsMat = true;

        public DroidCvPattern(Mat Mat, bool OwnsMat = true)
        {
            this.Mat = Mat;
            _ownsMat = OwnsMat;
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

        public void Dispose()
        {
            if (_ownsMat)
            {
                Mat.Release();
            }
        }

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
                    var region = new Region((int)loc.X, (int)loc.Y, Template.Width, Template.Height);

                    yield return new Match(region, score);

                    using var mask = new Mat();
                    Imgproc.FloodFill(result, mask, loc, new Scalar(0));
                }
                else break;
            }
        }

        public IPattern Copy()
        {
            return new DroidCvPattern(Mat.Clone());
        }
    }
}