using System;
using System.Globalization;
using Xamarin.Forms;

namespace FateGrandAutomata
{
    public class IntEnumConverter : IValueConverter
    {
        public object Convert(object Value, Type TargetType, object Parameter, CultureInfo Culture)
        {
            return Value is Enum ? (int) Value : 0;
        }

        public object ConvertBack(object Value, Type TargetType, object Parameter, CultureInfo Culture)
        {
            return Value is int ? Enum.ToObject(TargetType, Value) : 0;
        }
    }
}