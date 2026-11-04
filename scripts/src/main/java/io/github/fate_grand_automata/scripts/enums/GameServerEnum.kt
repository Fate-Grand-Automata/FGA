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

    private val packagesByServer = mapOf(
        GameServer.En.Original to listOf("com.aniplex.fategrandorder.en"),
        GameServer.En.BetterFGO to listOf("io.rayshift.betterfgo.en"),
        GameServer.Jp.Original to listOf("com.aniplex.fategrandorder"),
        GameServer.Jp.BetterFGO to listOf("io.rayshift.betterfgo"),
        GameServer.Cn to listOf(
            "com.bilibili.fatego",
            "com.bilibili.fatego.sharejoy",
            "com.bilibili.fgo.mi", // Xiaomi
            "com.bilibili.fgo.uc", // 9Game / UC
            "com.bilibili.fgo.vivo",
            "com.bilibili.fgo.m4399",
            "com.tencent.tmgp.fgo", // Tencent MyApp
            "com.bilibili.uo.fatego" // migration server
        ),
        GameServer.Tw to listOf("com.komoe.fgomycard", "com.xiaomeng.fategrandorder"),
        GameServer.Kr to listOf("com.netmarble.fgok")
    )

    val packageNames = packagesByServer
        .flatMap { (server, packages) -> packages.map { it to server } }
        .toMap()

    private val serializedValues = values.associateBy { it.serialize() }

    /**
     * Maps an APK package name to the corresponding [GameServer].
     */
    fun fromPackageName(packageName: String): GameServer? = packageNames[packageName]

    fun deserialize(value: String): GameServer? = serializedValues[value]
}
