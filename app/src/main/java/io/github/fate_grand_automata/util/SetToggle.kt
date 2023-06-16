package io.github.fate_grand_automata.util

fun <T> Set<T>.toggle(item: T) =
    if (item in this)
        this - item
    else this + item