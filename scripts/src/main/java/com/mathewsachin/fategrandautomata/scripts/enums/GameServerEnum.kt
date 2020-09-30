package com.mathewsachin.fategrandautomata.scripts.enums

enum class GameServerEnum constructor(vararg val packageNames: String) {
    En("com.aniplex.fategrandorder.en"),
    Jp("com.aniplex.fategrandorder"),
    Cn("com.bilibili.fatego", "com.bilibili.fatego.sharejoy"),
    Tw("com.komoe.fgomycard", "com.xiaomeng.fategrandorder"),
    Kr("com.netmarble.fgok");

    companion object {
        /**
         * Maps an APK package name to the corresponding [GameServerEnum].
         */
        fun fromPackageName(packageName: String): GameServerEnum? =
            values().find { packageName in it.packageNames }
    }
}