package io.github.lib_automata

sealed class CompareBy {
    class Width(val width: Int) : CompareBy()
    class Height(val height: Int) : CompareBy()
    object None : CompareBy()
}