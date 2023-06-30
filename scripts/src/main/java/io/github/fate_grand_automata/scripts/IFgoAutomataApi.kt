package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.locations.Locations
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.lib_automata.AutomataApi

interface IFgoAutomataApi : AutomataApi {
    val prefs: IPreferences
    val images: IImageLoader
    val locations: Locations
    val messages: IScriptMessages
}