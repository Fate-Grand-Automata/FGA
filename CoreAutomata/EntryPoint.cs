using System;
using System.Threading;

namespace CoreAutomata
{
    public abstract class EntryPoint
    {
        Thread _loopThread;

        public void Run()
        {
            if (_loopThread != null)
                return;

            _loopThread = new Thread(ScriptRunner);
            _loopThread.Start();
        }

        public void Stop()
        {
            _loopThread?.Abort();
        }

        static bool LogError(Exception E)
        {
            AutomataApi.WriteDebug(E.ToString());

            return false;
        }

        void ScriptRunner()
        {
            try
            {
                Script();
            }
            catch (ThreadAbortException)
            {
                AutomataApi.ShowMessageBox("User Aborted", "Script stopped by user");
            }
            catch (ScriptExitException e)
            {
                ScriptExit?.Invoke(e.Message);

                AutomataApi.ShowMessageBox("Script Exited", e.Message);
            }
            catch (Exception e)
            {
                LogError(e);

                ScriptExit?.Invoke(e.Message);

                AutomataApi.ShowMessageBox("Unexpected Error", e.Message);
            }
        }

        protected abstract void Script();

        public event Action<string> ScriptExit;
    }
}