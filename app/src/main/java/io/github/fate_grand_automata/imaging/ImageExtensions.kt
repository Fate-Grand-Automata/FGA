package io.github.fate_grand_automata.imaging

import android.graphics.Bitmap
import org.opencv.core.Mat

inline fun <T> Bitmap.use(block: (Bitmap) -> T) =
    try {
        block(this)
    } finally {
        recycle()
    }

inline fun <T : Mat, R> T.use(block: (T) -> R) =
    try {
        block(this)
    } finally {
        release()
    }
