package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum

data class FgoPackageNames(val server: GameServerEnum, val pkgName: String)

val fgoPackageNames = listOf(
    FgoPackageNames(GameServerEnum.En, "com.aniplex.fategrandorder.en"),
    FgoPackageNames(GameServerEnum.Jp, "com.aniplex.fategrandorder")
)