package com.mathewsachin.fategrandautomata.scripts.locations

import com.mathewsachin.fategrandautomata.scripts.isWide
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.PlatformImpl
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.ITransformationExtensions
import javax.inject.Inject

@ScriptScope
class ScriptAreaTransforms @Inject constructor(
    prefs: IPreferences,
    transformations: ITransformationExtensions,
    gameAreaManager: GameAreaManager,
    platformImpl: PlatformImpl
) : IScriptAreaTransforms {
    override val scriptArea =
        Region(
            Location(),
            gameAreaManager.gameArea.size * (1 / transformations.scriptToScreenScale())
        )

    override val isWide = prefs.isNewUI && scriptArea.size.isWide()

    override val isNewUI = prefs.isNewUI

    override val gameServer = prefs.gameServer

    override val canLongSwipe = platformImpl.canLongSwipe

    override fun Location.xFromCenter() =
        this + Location(scriptArea.center.x, 0)

    override fun Region.xFromCenter() =
        this + Location(scriptArea.center.x, 0)

    override fun Location.xFromRight() =
        this + Location(scriptArea.right, 0)

    override fun Region.xFromRight() =
        this + Location(scriptArea.right, 0)

    override fun Location.yFromBottom() =
        this + Location(0, scriptArea.bottom)

    override fun Region.yFromBottom() =
        this + Location(0, scriptArea.bottom)
}