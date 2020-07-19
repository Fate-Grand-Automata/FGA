package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.IAutomataExtensions

interface IFGAutomataApi : IAutomataExtensions {
    val prefs: IPreferences
    val images: ImageLocator
}