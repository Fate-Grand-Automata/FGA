using System;

namespace CoreAutomata
{
    public static class ComparableExtensions
    {
        /// <summary>
        /// Ensures that a <see cref="Value"/> is between the specified <see cref="Minimum"/> and <see cref="Maximum"/>.
        /// </summary>
        public static T Clip<T>(this T Value, T Minimum, T Maximum) where T : IComparable<T>
        {
            if (Value.CompareTo(Minimum) < 0)
                return Minimum;

            if (Value.CompareTo(Maximum) > 0)
                return Maximum;

            return Value;
        }

        /// <summary>
        /// Rounds a double value to integer.
        /// </summary>
        public static int Round(this double Value) => (int) Math.Round(Value);

        /// <summary>
        /// Rounds a float value to integer.
        /// </summary>
        public static int Round(this float Value) => (int) Math.Round(Value);
    }
}