package com.mathewsachin.fategrandautomata.imaging

import org.opencv.core.Mat

class DisposableMat(var Mat: Mat?): AutoCloseable {
    init {
        require(Mat != null){ "Mat should not be null" }
    }

    constructor(): this(Mat())

    override fun close() {
        Mat?.release()
        Mat = null
    }
}