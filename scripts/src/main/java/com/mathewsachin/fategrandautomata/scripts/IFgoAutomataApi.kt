package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.locations.Locations
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.extensions.IAutomataExtensions

interface IFgoAutomataApi : IAutomataExtensions {
    val prefs: IPreferences
    val images: IImageLoader
    val game: Locations
    val messages: IScriptMessages
}