using System;
using System.IO;
using System.Text;
using Java.Lang;
using Exception = System.Exception;

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

                // We can check if we got root by sending 'id' to the su process which returns current user's id.
                // The response should contain 'uid=0', where 0 is the id of root user.
                _superUserStreamWriter.WriteLine("id");
                _superUserStreamWriter.Flush();

                // Don't dispose reader or InputStream will be closed
                var reader = new StreamReader(_superUser.InputStream, Encoding.ASCII);
                var response = reader.ReadLine();
                if (response == null || !response.Contains("uid=0"))
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                throw new Exception("Failed to get Root permission", e);
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