using System;
using System.Collections.Generic;

namespace FateGrandAutomata
{
    public class PreferencesViewHelper
    {
        PreferencesViewHelper() { }

        public static PreferencesViewHelper Instance { get; } = new PreferencesViewHelper();

        public IEnumerable<string> RefillResourceNames { get; } = Enum
            .GetNames(typeof(RefillResource));
    }
}