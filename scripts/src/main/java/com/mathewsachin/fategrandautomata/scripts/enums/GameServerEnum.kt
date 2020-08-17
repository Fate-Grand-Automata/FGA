package com.mathewsachin.fategrandautomata.scripts.enums

enum class GameServerEnum constructor(val packageName: String) {
    En("com.aniplex.fategrandorder.en"),
    Jp("com.aniplex.fategrandorder"),
    Cn("com.bilibili.fatego.sharejoy"),
    Tw("com.komoe.fgomycard"),
    Kr("com.netmarble.fgok");

    companion object {
        /**
         * Maps an APK package name to the corresponding [GameServerEnum].
         */
        fun fromPackageName(packageName: String): GameServerEnum? =
            values().find { it.packageName == packageName }
    }
}