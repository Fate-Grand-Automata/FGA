package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import java.io.File

fun EditTextPreference.makeNumeric() {
    setOnBindEditTextListener {
        it.inputType = InputType.TYPE_CLASS_NUMBER
    }
}

fun EditTextPreference.makeMultiLine() {
    setOnBindEditTextListener {
        it.isSingleLine = false
    }
}

fun MultiSelectListPreference.populateFriendOrCe(ImgFolder: File) {
    val entries = (ImgFolder.listFiles() ?: emptyArray())
        .filter { it.isFile }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    // actual values
    this.entryValues = entries
        .map { it.name }
        .toTypedArray()

    // labels
    this.entries = entries
        .map { it.nameWithoutExtension }
        .toTypedArray()
}

inline fun <reified T : Enum<T>> MultiSelectListPreference.initWith(localized: (T) -> Int) {
    val values = enumValues<T>()

    this.entryValues = values
        .map { it.toString() }
        .toTypedArray()

    this.entries = values
        .map { context.getString(localized(it)) }
        .toTypedArray()
}

inline fun <reified T : Enum<T>> ListPreference.initWith(localized: (T) -> Int) {
    val values = enumValues<T>()

    this.entryValues = values
        .map { it.toString() }
        .toTypedArray()

    this.entries = values
        .map { context.getString(localized(it)) }
        .toTypedArray()
}