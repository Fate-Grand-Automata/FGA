package io.github.lib_automata

interface OcrService {
    fun detectText(pattern: Pattern): String

    /**
     * Cleans up resources used by the OCR service.
     *
     * This method should be called when the OCR service is no longer needed
     * to release any resources (e.g., memory, file handles, or other system resources)
     * that may have been allocated during its operation.
     */
    fun close()
}