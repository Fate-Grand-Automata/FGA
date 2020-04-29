using System.IO;
using System.Text;
using Android.OS;
using CoreAutomata;
using Java.Lang;
using Org.Opencv.Core;
using Org.Opencv.Imgproc;
using Process = Java.Lang.Process;

namespace FateGrandAutomata
{
    public class RootScreenshotService : IScreenshotService
    {
        readonly Process _superUser;
        readonly StreamWriter _superUserStreamWriter;
        byte[] _buffer;
        Mat _rootLoadMat, _rootConvertMat = new Mat();

        readonly string _imgPath;

        public RootScreenshotService()
        {
            try
            {
                _superUser = Runtime.GetRuntime().Exec("su", null, null);
                _superUserStreamWriter = new StreamWriter(_superUser.OutputStream, Encoding.ASCII);
            }
            catch (Exception e)
            {
                throw new System.Exception("Failed to get Root permission", e);
            }


            _imgPath = Path.Combine(AutomataApi.StorageDir, "sshot.raw");
        }

        public IPattern TakeScreenshot()
        {
            _superUserStreamWriter.WriteLine($"/system/bin/screencap {_imgPath}");
            _superUserStreamWriter.Flush();

            // Wait
            // https://stackoverflow.com/a/16160785/5377194
            {
                _superUserStreamWriter.WriteLine("echo -n 0");
                _superUserStreamWriter.Flush();

                _superUser.InputStream.ReadByte();
            }

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

            _superUserStreamWriter.WriteLine("exit");
            _superUserStreamWriter.Flush();
            _superUserStreamWriter.Dispose();

            _superUser.WaitFor();
        }
    }
}