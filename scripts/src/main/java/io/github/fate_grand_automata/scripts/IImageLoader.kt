package io.github.fate_grand_automata.scripts

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.enums.GameServer
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.lib_automata.Pattern

interface IImageLoader {
    operator fun get(img: Images, gameServer: GameServer? = null): Pattern

    fun loadSupportPattern(kind: SupportImageKind, name: String): List<Pattern>

    fun loadMaterial(material: MaterialEnum): Pattern

    fun clearImageCache()

    fun clearSupportCache()
}
