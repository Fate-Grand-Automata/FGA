using System;

namespace CoreAutomata
{
    public interface IScreenshotService : IDisposable
    {
        IPattern TakeScreenshot();
    }
}