package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.locations.Locations
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.lib_automata.AutomataApi
import javax.inject.Inject

class FgoAutomataApi @Inject constructor(
    automataApi: AutomataApi,
    override val prefs: IPreferences,
    override val images: IImageLoader,
    override val locations: Locations,
    override val messages: IScriptMessages
) : IFgoAutomataApi, AutomataApi by automataApi