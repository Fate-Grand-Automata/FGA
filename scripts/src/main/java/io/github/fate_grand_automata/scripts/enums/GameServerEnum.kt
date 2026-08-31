package io.github.fate_grand_automata.scripts.enums

private const val betterFgoSuffix = " BFGO"

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
}

/**
 * The servers the app knows about, in the order they are offered to the user.
 *
 * Kept out of [GameServer] itself: naming a subclass from that class's own initializer reads it
 * while it is still being created, whenever a subclass is what triggered the initializer.
 */
object GameServers {
    // Widened on purpose - inference would pin this to the type of the one object it holds
    val default: GameServer = GameServer.En.Original

    val values = listOf(
        GameServer.En.Original,
        GameServer.En.BetterFGO,
        GameServer.Jp.Original,
        GameServer.Jp.BetterFGO,
        GameServer.Cn,
        GameServer.Tw,
        GameServer.Kr
    )

    val packageNames = mapOf(
        "com.aniplex.fategrandorder.en" to GameServer.En.Original,
        "io.rayshift.betterfgo.en" to GameServer.En.BetterFGO,
        "com.aniplex.fategrandorder" to GameServer.Jp.Original,
        "io.rayshift.betterfgo" to GameServer.Jp.BetterFGO,
        "com.bilibili.fatego" to GameServer.Cn,
        "com.bilibili.fatego.sharejoy" to GameServer.Cn,
        "com.komoe.fgomycard" to GameServer.Tw,
        "com.xiaomeng.fategrandorder" to GameServer.Tw,
        "com.netmarble.fgok" to GameServer.Kr
    )

    private val serializedValues = values.associateBy { it.serialize() }

    /**
     * Maps an APK package name to the corresponding [GameServer].
     */
    fun fromPackageName(packageName: String): GameServer? = packageNames[packageName]

    fun deserialize(value: String): GameServer? = serializedValues[value]
}
