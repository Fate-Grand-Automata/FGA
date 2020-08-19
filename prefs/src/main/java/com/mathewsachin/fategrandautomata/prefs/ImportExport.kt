package com.mathewsachin.fategrandautomata.prefs

import android.content.SharedPreferences

fun SharedPreferences.Editor.import(map: Map<String, *>) {
    for ((key, value) in map) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Collection<*> -> putStringSet(key, value.map { it.toString() }.toSet())
        }
    }
}