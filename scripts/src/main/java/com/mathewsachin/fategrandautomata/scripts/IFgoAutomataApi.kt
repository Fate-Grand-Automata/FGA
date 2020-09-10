package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.libautomata.extensions.IAutomataExtensions

interface IFgoAutomataApi : IAutomataExtensions {
    val prefs: IPreferences
    val images: ImageLocator
    val game: Game
    val messages: IScriptMessages
}