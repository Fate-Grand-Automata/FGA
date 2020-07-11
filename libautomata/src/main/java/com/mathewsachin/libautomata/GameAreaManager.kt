package com.mathewsachin.libautomata

/**
 * This class is used for storing the script and image dimensions and for storing the playable area
 * in [gameArea].
 */
class GameAreaManager(val platformImpl: IPlatformImpl) {
    /**
     * Stores either the width or height of the script dimensions. Normally, this is 1440p.
     */
    var scriptDimension: CompareSettings? = null

    /**
     * Stores either the width or height of the image dimensions. Normally, this is 720p.
     */
    var compareDimension: CompareSettings? = null

    private var _gameArea: Region? = null

    /**
     * Stores the playable area as a [Region] in image coordinates, which is normally 720p.
     * Notches and the blue bars are excluded from this area by introducing X and Y offsets.
     */
    var gameArea: Region
        get() {
            // store the WindowRegion so we don't calculate it twice
            val windowRegion = platformImpl.windowRegion
            return _gameArea?.plus(windowRegion.location) ?: windowRegion
        }
        set(value) {
            _gameArea = value
        }

    /**
     * Sets all fields to null.
     */
    fun reset() {
        scriptDimension = null
        compareDimension = null
        _gameArea = null
    }
}