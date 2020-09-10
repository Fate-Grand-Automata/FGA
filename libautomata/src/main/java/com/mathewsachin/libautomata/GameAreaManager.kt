package com.mathewsachin.libautomata

/**
 * This class is used for storing the script and image dimensions and for storing the playable area
 * in [gameArea].
 */
interface GameAreaManager {
    /**
     * Stores either the width or height of the script dimensions. Normally, this is 1440p.
     */
    val scriptDimension: CompareBy

    /**
     * Stores either the width or height of the image dimensions. Normally, this is 720p.
     */
    val compareDimension: CompareBy

    /**
     * Stores the playable area as a [Region] in image coordinates, which is normally 720p.
     * Notches and the blue bars are excluded from this area by introducing X and Y offsets.
     */
    val gameArea: Region
}