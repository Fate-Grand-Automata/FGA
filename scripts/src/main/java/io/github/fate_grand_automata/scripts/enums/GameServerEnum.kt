package io.github.fate_grand_automata.scripts.enums

sealed class GameServer constructor(val simple: String, val betterFgo: Boolean = false) {
    sealed class En constructor(betterFgo: Boolean = false) : GameServer("En", betterFgo) {
        object Original : En()
        object BetterFGO : En(true)
    }

    sealed class Jp constructor(betterFgo: Boolean = false) : GameServer("Jp", betterFgo) {
        object Original : Jp()
        object BetterFGO : Jp(true)
    }

    object Cn : GameServer("Cn")
    object Tw : GameServer("Tw")
    object Kr : GameServer("Kr")

    fun serialize(): String = simple + (if (betterFgo) betterFgoSuffix else "")

    override fun toString(): String = serialize()

    companion object {
        val default = En.Original as GameServer

        private const val betterFgoSuffix = " BFGO"

        /**
         * Maps an APK package name to the corresponding [GameServer].
         */
        fun fromPackageName(packageName: String): GameServer? = packageNames.get(packageName)

        val values = listOf(
            En.Original,
            En.BetterFGO,
            Jp.Original,
            Jp.BetterFGO,
            Cn, Tw, Kr
        )

        val packageNames = mapOf(
            "com.aniplex.fategrandorder.en" to En.Original,
            "io.rayshift.betterfgo.en" to En.BetterFGO,
            "com.aniplex.fategrandorder" to Jp.Original,
            "io.rayshift.betterfgo" to Jp.BetterFGO,
            "com.bilibili.fatego" to Cn,
            "com.bilibili.fatego.sharejoy" to Cn,
            "com.komoe.fgomycard" to Tw,
            "com.xiaomeng.fategrandorder" to Tw,
            "com.netmarble.fgok" to Kr
        )

        private val serializedValues by lazy {
            values.associateBy { it.serialize() }
        }

        fun deserialize(value: String): GameServer? = serializedValues[value]
    }
}