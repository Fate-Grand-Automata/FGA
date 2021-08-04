package com.mathewsachin.fategrandautomata.imaging

import org.opencv.core.Mat

class DisposableMat(var mat: Mat? = Mat()) : AutoCloseable {
    init {
        require(mat != null) { "Mat should not be null" }
    }

    constructor() : this(Mat())

    override fun close() {
        mat?.release()
        mat = null
    }
}