package io.github.lib_automata

interface OcrService {
    fun detectText(pattern: Pattern): String

    fun detectNumVarBg(pattern: Pattern): String
}