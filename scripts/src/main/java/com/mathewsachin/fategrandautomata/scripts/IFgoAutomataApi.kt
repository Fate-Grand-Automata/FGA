package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.locations.Locations
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.AutomataApi

interface IFgoAutomataApi : AutomataApi {
    val prefs: IPreferences
    val images: IImageLoader
    val locations: Locations
    val messages: IScriptMessages
}