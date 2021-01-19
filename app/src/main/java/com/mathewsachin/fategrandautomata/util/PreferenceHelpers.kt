package com.mathewsachin.fategrandautomata.util

import android.text.InputType
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.SupportImageKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import timber.log.error
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

suspend fun MultiSelectListPreference.populateFriendOrCe(storageProvider: IStorageProvider, kind: SupportImageKind) {
    val entries = try {
        withContext(Dispatchers.IO) {
            storageProvider.list(kind)
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
        }
    } catch (e: Exception) {
        val msg = "Couldn't access Support images"

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        Timber.error(e) { msg }

        emptyList()
    }

    // actual values
    this.entryValues = entries
        .toTypedArray()

    // labels
    this.entries = entries
        .map { File(it).nameWithoutExtension }
        .toTypedArray()
}

inline fun <reified T : Enum<T>> MultiSelectListPreference.initWith(localized: (T) -> Int) = apply {
    val values = enumValues<T>()

    this.entryValues = values
        .map { it.toString() }
        .toTypedArray()

    this.entries = values
        .map { context.getString(localized(it)) }
        .toTypedArray()
}

inline fun <reified T : Enum<T>> ListPreference.initWith(localized: (T) -> Int) = apply {
    val values = enumValues<T>()

    this.entryValues = values
        .map { it.toString() }
        .toTypedArray()

    this.entries = values
        .map { context.getString(localized(it)) }
        .toTypedArray()
}