package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.IAutomataExtensions

class FGAutomataApi(
    automataApi: IAutomataExtensions,
    override val prefs: IPreferences,
    override val images: ImageLocator
) : IFGAutomataApi, IAutomataExtensions by automataApi {

}