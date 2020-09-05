package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.extensions.IAutomataExtensions
import javax.inject.Inject

class FGAutomataApi @Inject constructor(
    automataApi: IAutomataExtensions,
    override val prefs: IPreferences,
    override val images: ImageLocator,
    override val game: Game,
    override val messages: IScriptMessages
) : IFGAutomataApi, IAutomataExtensions by automataApi