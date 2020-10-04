package com.mathewsachin.libautomata

/**
 * Represents an image search match, containing the match area and the matching score.
 */
data class Match(val Region: Region, val score: Double) : Comparable<Match> {
    override fun compareTo(other: Match) =
        Region.compareTo(other.Region)
}