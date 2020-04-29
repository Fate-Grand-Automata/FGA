using System.IO;
using System.Text;
using Android.OS;
using CoreAutomata;
using Org.Opencv.Core;
using Org.Opencv.Imgproc;

namespace FateGrandAutomata
{
    public class RootScreenshotService : IScreenshotService
    {
        readonly SuperUser _superUser;
        byte[] _buffer;
        Mat _rootLoadMat, _rootConvertMat = new Mat();

        readonly string _imgPath;

        public RootScreenshotService(SuperUser SuperUser)
        {
            _superUser = SuperUser;

            _imgPath = Path.Combine(AutomataApi.StorageDir, "sshot.raw");
        }

        public IPattern TakeScreenshot()
        {
            _superUser.SendCommand($"/system/bin/screencap {_imgPath}");

            using var f = File.OpenRead(_imgPath);
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

        public void Dispose()
        {
            _rootLoadMat?.Release();
            _rootLoadMat = null;

            _rootConvertMat?.Release();
            _rootConvertMat = null;

            _buffer = null;
        }
    }
}