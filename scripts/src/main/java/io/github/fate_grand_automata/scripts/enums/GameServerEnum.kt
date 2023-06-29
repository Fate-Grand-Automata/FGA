package io.github.fate_grand_automata.scripts.enums

sealed class GameServer constructor(val betterFgo: Boolean = false) {
    sealed class En constructor(betterFgo: Boolean = false) : GameServer(betterFgo) {
        object Original : En()
        object BetterFGO : En(true)
    }

    sealed class Jp constructor(betterFgo: Boolean = false) : GameServer(betterFgo) {
        object Original : Jp()
        object BetterFGO : Jp(true)
    }

    object Cn : GameServer()
    object Tw : GameServer()
    object Kr : GameServer()

    fun serialize(): String = when (this) {
        is En -> En::class.simpleName
        is Jp -> Jp::class.simpleName
        Cn, Tw, Kr -> this.javaClass.simpleName
    } + (if (betterFgo) betterFgoSuffix else "")


    companion object {
        val default = En.Original as GameServer

        private val betterFgoSuffix = " BFGO"

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

        fun deserialize(value: String): GameServer? =
            when (value) {
                En::class.simpleName -> En.Original
                En::class.simpleName + betterFgoSuffix -> En.BetterFGO
                Jp::class.simpleName -> Jp.Original
                Jp::class.simpleName + betterFgoSuffix -> Jp.BetterFGO
                else -> {
                    GameServer::class.sealedSubclasses.firstOrNull {
                        it.simpleName == value
                    }?.objectInstance
                }
            }
    }
}