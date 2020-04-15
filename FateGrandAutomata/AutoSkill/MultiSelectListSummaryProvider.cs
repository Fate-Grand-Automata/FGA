using AndroidX.Preference;
using Java.Lang;

namespace FateGrandAutomata
{
    class MultiSelectListSummaryProvider : Object, Preference.ISummaryProvider
    {
        public ICharSequence ProvideSummaryFormatted(Preference Pref)
        {
            if (Pref is MultiSelectListPreference list)
            {
                var str = list.Values.Count > 0
                    ? string.Join(", ", list.Values)
                    : "Any";

                return new String(str);
            }

            return new String();
        }
    }
}