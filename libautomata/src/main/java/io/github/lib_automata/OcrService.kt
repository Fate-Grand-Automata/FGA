package io.github.lib_automata

interface OcrService {
    fun detectText(pattern: Pattern): String

    /**
     * Extracts and returns the numeric value inside parentheses from the given [pattern].
     *
     * Only the first occurrence is returned.
     *
     * @param pattern The image pattern to analyze.
     * @return The numeric string inside the first matched parentheses, or an empty string if none found.
     */
    fun detectNumberInBrackets(pattern: Pattern): String
}