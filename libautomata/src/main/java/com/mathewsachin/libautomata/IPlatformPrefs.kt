package com.mathewsachin.libautomata

interface IPlatformPrefs {
    val debugMode: Boolean

    /**
     * The default minimum similarity used for image comparisons.
     */
    val minSimilarity: Double
}