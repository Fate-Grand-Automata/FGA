using System;
using System.IO;
using System.Text;
using Java.Lang;
using Exception = Java.Lang.Exception;

namespace FateGrandAutomata
{
    public class SuperUser : IDisposable
    {
        readonly Process _superUser;
        readonly StreamWriter _superUserStreamWriter;

        public SuperUser()
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
        }

        void WaitForCommand()
        {
            // https://stackoverflow.com/a/16160785/5377194
            _superUserStreamWriter.WriteLine("echo -n 0");
            _superUserStreamWriter.Flush();

            _superUser.InputStream.ReadByte();
        }

        public void SendCommand(string Command)
        {
            _superUserStreamWriter.WriteLine(Command);
            _superUserStreamWriter.Flush();

            WaitForCommand();
        }

        public void Dispose()
        {
            _superUserStreamWriter.WriteLine("exit");
            _superUserStreamWriter.Flush();
            _superUserStreamWriter.Dispose();

            _superUser.WaitFor();
        }
    }
}