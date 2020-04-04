using Android.Text;
using Android.Widget;
using AndroidX.Preference;

namespace FateGrandAutomata
{
    public static class PreferenceExtensions
    {
        class NumericOnBindEditTextListener : Java.Lang.Object, EditTextPreference.IOnBindEditTextListener
        {
            public void OnBindEditText(EditText EditText)
            {
                EditText.InputType = InputTypes.ClassNumber;
            }
        }

        public static void MakeNumeric(this EditTextPreference Preference)
        {
            Preference.SetOnBindEditTextListener(new NumericOnBindEditTextListener());
        }
    }
}