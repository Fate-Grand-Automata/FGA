using System.Collections.Generic;
using System.IO;
using CoreAutomata;
using Org.Opencv.Core;
using Org.Opencv.Imgcodecs;
using Org.Opencv.Imgproc;
using Size = CoreAutomata.Size;

namespace FateGrandAutomata
{
    public class DroidCvPattern : IPattern
    {
        public DroidCvPattern(Mat Mat)
        {
            this.Mat = Mat;
        }

        public DroidCvPattern(Stream Stream)
        {
            var buffer = new byte[Stream.Length];
            using var ms = new MemoryStream(buffer);
            Stream.CopyTo(ms);

            using var raw = new MatOfByte(buffer);

            Mat = Imgcodecs.Imdecode(raw, 0);
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

        public bool IsMatch(IPattern Template, double Similarity)
        {
            var result = new Mat();

            // min is used for tmsqdiffnormed
            Imgproc.MatchTemplate(Mat, (Template as DroidCvPattern)?.Mat, result, Imgproc.TmSqdiffNormed);

            var minMaxLocResult = Core.MinMaxLoc(result);

            return minMaxLocResult.MinVal <= (1 - Similarity);
        }

        public int Width => Mat.Width();

        public int Height => Mat.Height();

        public IEnumerable<Match> FindMatches(IPattern Template, double Similarity)
        {
            var result = new Mat();

            // max is used for tmccoeff
            Imgproc.MatchTemplate(Mat, (Template as DroidCvPattern)?.Mat, result, Imgproc.TmCcoeff);

            for (var x = 0; x < result.Width(); ++x)
            {
                for (var y = 0; y < result.Height(); ++y)
                {
                    var score = result.Get(x, y)[0];

                    if (score >= Similarity)
                    {
                        var location = new Region(x, y, Template.Width, Template.Height);
                        yield return new Match(location, score);
                    }
                }
            }
        }
    }
}