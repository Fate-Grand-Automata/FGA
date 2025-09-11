package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.isWide
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.lib_automata.GameAreaManager
import io.github.lib_automata.Location
import io.github.lib_automata.PlatformImpl
import io.github.lib_automata.Region
import io.github.lib_automata.Scale
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ScriptAreaTransforms @Inject constructor(
    prefs: IPreferences,
    scale: Scale,
    gameAreaManager: GameAreaManager,
    platformImpl: PlatformImpl,
) : IScriptAreaTransforms {
    override val scriptArea =
        Region(
            Location(),
            gameAreaManager.gameArea.size * (1 / scale.scriptToScreen),
        )

    override val isWide = scriptArea.size.isWide()

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
