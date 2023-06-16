package io.github.fate_grand_automata.scripts.locations

import io.github.fate_grand_automata.scripts.enums.GameServerEnum
import io.github.lib_automata.Location
import io.github.lib_automata.Region

interface IScriptAreaTransforms {
    val scriptArea: Region
    val isWide: Boolean
    val gameServer: GameServerEnum
    val canLongSwipe: Boolean
    fun Location.xFromCenter(): Location
    fun Region.xFromCenter(): Region
    fun Location.xFromRight(): Location
    fun Region.xFromRight(): Region
    fun Location.yFromBottom(): Location
    fun Region.yFromBottom(): Region
}