using System;

namespace FateGrandAutomata
{
    public static class GestureTimings
    {
        public static TimeSpan ClickWaitTime { get; } = TimeSpan.FromSeconds(0.3);

        public static TimeSpan ClickDuration { get; } = TimeSpan.FromMilliseconds(50);

        public static TimeSpan ClickDelay { get; } = TimeSpan.FromMilliseconds(10);

        public static TimeSpan SwipeDuration { get; } = TimeSpan.FromMilliseconds(300);

        public static TimeSpan SwipeWaitTime { get; } = TimeSpan.FromSeconds(0.7);
    }
}