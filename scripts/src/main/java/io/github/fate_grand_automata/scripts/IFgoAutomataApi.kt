package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.locations.Locations
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.lib_automata.AutomataApi
import io.github.lib_automata.Region
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface IFgoAutomataApi : AutomataApi {
    val prefs: IPreferences
    val images: IImageLoader
    val locations: Locations
    val messages: IScriptMessages

    /**
     * Helper method to search for the current server's image and to also search for the English one in case of TranslateFGO.
     */
    fun findImage(region: Region, image: Images) =
        images[image] in region || (prefs.gameServer is GameServer.Jp && images[image, GameServer.default] in region)

    fun findImage(region: Region, image: Images, timeout: Duration) =
        region.exists(images[image], timeout = timeout) ||
            (prefs.gameServer is GameServer.Jp && region.exists(images[image, GameServer.default], timeout = 0.5.seconds))
}