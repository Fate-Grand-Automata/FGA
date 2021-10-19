package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.locations.Locations
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.extensions.IAutomataExtensions
import javax.inject.Inject

class FgoAutomataApi @Inject constructor(
    automataApi: IAutomataExtensions,
    override val prefs: IPreferences,
    override val images: IImageLoader,
    override val locations: Locations,
    override val messages: IScriptMessages
) : IFgoAutomataApi, IAutomataExtensions by automataApi