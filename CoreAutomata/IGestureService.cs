﻿using System;

namespace CoreAutomata
{
    public interface IGestureService : IDisposable
    {
        void Swipe(Location Start, Location End);

        void Click(Location Location);

        void ContinueClick(Location Location, int Times);
    }
}