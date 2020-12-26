package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.extensions.IAutomataExtensions
import javax.inject.Inject

class FgoAutomataApi @Inject constructor(
    automataApi: IAutomataExtensions,
    override val prefs: IPreferences,
    override val images: ImageLocator,
    override val game: Game,
    override val messages: IScriptMessages
) : IFgoAutomataApi, IAutomataExtensions by automataApi