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

        public DroidCvPattern() : this(new Mat()) { }

        public DroidCvPattern(Mat Mat, bool OwnsMat = true)
        {
            this.Mat = Mat;
            _ownsMat = OwnsMat;
        }

        public DroidCvPattern(Stream Stream, bool MakeMask)
        {
            var buffer = new byte[Stream.Length];
            using var ms = new MemoryStream(buffer);
            Stream.CopyTo(ms);

            using var raw = new DisposableMat(new MatOfByte(buffer));

            if (MakeMask)
            {
                using var rgbaMat = new DisposableMat(Imgcodecs.Imdecode(raw.Mat, Imgcodecs.CvLoadImageUnchanged));

                Mat = new Mat();
                Imgproc.CvtColor(rgbaMat.Mat, Mat, Imgproc.ColorRgba2gray);

                Mask = new Mat();
                // Extract alpha channel
                Core.ExtractChannel(rgbaMat.Mat, Mask, 3);
                // Mask containing 0 or 255
                Imgproc.Threshold(Mask, Mask, 0, 255, Imgproc.ThreshBinary);
            }
            else
            {
                Mat = Imgcodecs.Imdecode(raw.Mat, Imgcodecs.CvLoadImageGrayscale);
            }
        }

        public Mat Mat { get; }

        public Mat Mask { get; }

        public void Dispose()
        {
            if (_ownsMat)
            {
                Mat.Release();
            }

            Mask?.Release();
        }

        public IPattern Resize(Size Size)
        {
            var result = new Mat();

            Resize(result, Size);

            return new DroidCvPattern(result);
        }

        void Resize(Mat Target, Size Size)
        {
            Imgproc.Resize(Mat, Target, new Org.Opencv.Core.Size(Size.Width, Size.Height), 0, 0, Imgproc.InterArea);
        }

        public void Resize(IPattern Target, Size Size)
        {
            if (Target is DroidCvPattern target)
            {
                Resize(target.Mat, Size);
            }
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

        public int Width => Mat.Width();

        public int Height => Mat.Height();

        DisposableMat Match(DroidCvPattern Template)
        {
            var result = new DisposableMat();

            if (Template.Mask != null)
            {
                Imgproc.MatchTemplate(Mat, Template.Mat, result.Mat, Imgproc.TmCcorrNormed, Template.Mask);
            }
            else
            {
                Imgproc.MatchTemplate(Mat, Template.Mat, result.Mat, Imgproc.TmCcoeffNormed);
            }

            return result;
        }

        public bool IsMatch(IPattern Template, double Similarity)
        {
            using var result = Match(Template as DroidCvPattern);

            using var minMaxLocResult = Core.MinMaxLoc(result.Mat);

            return minMaxLocResult.MaxVal >= Similarity;
        }

        public IEnumerable<Match> FindMatches(IPattern Template, double Similarity)
        {
            using var result = Match(Template as DroidCvPattern);

            while (true)
            {
                using var minMaxLocResult = Core.MinMaxLoc(result.Mat);
                var score = minMaxLocResult.MaxVal;

                if (score >= Similarity)
                {
                    var loc = minMaxLocResult.MaxLoc;
                    var region = new Region((int)loc.X, (int)loc.Y, Template.Width, Template.Height);

                    yield return new Match(region, score);

                    using var mask = new DisposableMat();
                    // Flood fill eliminates the problem of nearby points to a high similarity point also having high similarity
                    const double floodFillDiff = 0.05;
                    Imgproc.FloodFill(result.Mat, mask.Mat, loc, new Scalar(0),
                        new Rect(),
                        new Scalar(floodFillDiff), new Scalar(floodFillDiff),
                        0);
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