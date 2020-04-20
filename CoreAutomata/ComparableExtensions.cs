using System;

namespace CoreAutomata
{
    public static class ComparableExtensions
    {
        public static T Clip<T>(this T Value, T Minimum, T Maximum) where T : IComparable<T>
        {
            if (Value.CompareTo(Minimum) < 0)
                return Minimum;

            if (Value.CompareTo(Maximum) > 0)
                return Maximum;

            return Value;
        }

        public static int Round(this double Value) => (int) Math.Round(Value);

        public static int Round(this float Value) => (int) Math.Round(Value);
    }
}