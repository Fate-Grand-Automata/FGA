package io.github.fate_grand_automata.scripts.prefs

import kotlin.time.Duration

interface ISupportPreferencesCommon {
    val mlbSimilarity: Double
    val swipesPerUpdate: Int
    val maxUpdates: Int
    val skillDisplayWait: Duration
    val appendUnlockedMinRatio: Double
    val classScoreMinRatio: Double
}
