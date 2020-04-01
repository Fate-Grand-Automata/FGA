using System;

namespace CoreAutomata
{
    public class ScriptExitException : Exception
    {
        public ScriptExitException(string Message) : base(Message)
        {
        }
    }
}