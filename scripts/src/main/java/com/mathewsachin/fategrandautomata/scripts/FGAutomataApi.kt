package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.modules.Scaling
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.IAutomataExtensions

class FGAutomataApi(
    automataApi: IAutomataExtensions,
    override val prefs: IPreferences,
    override val images: ImageLocator,
    override val game: Game,
    override val scaling: Scaling
) : IFGAutomataApi, IAutomataExtensions by automataApi {

}