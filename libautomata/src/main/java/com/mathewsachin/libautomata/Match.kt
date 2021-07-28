package com.mathewsachin.libautomata

/**
 * Represents an image search match, containing the match area and the matching score.
 */
data class Match(val region: Region, val score: Double) : Comparable<Match> {
    override fun compareTo(other: Match) =
        region.compareTo(other.region)
}