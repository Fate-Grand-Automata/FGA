package io.github.lib_automata

/**
 * OCR engines hold native resources, so callers must [close] the service when they're done
 * with it instead of waiting for the garbage collector to get around to it.
 */
interface OcrService : AutoCloseable {
    fun detectText(pattern: Pattern): String
}