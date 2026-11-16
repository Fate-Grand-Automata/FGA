package io.github.fate_grand_automata.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import io.github.fate_grand_automata.R
import java.io.PrintWriter
import java.io.StringWriter

val Exception.messageAndStackTrace
    get(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        return "${message}\n\n${sw}"
    }

/**
 * The localized message of a [KnownException], otherwise a generic error with this exception's
 * message.
 */
fun Exception?.displayMessage(context: Context): String =
    if (this is KnownException) reason.msg(context)
    else "${context.getString(R.string.unexpected_error)}: ${this?.message}"

fun ClipboardManager.set(context: Context, e: Exception) {
    val clipData = ClipData.newPlainText(
        context.getString(R.string.unexpected_error),
        e.messageAndStackTrace
    )

    setPrimaryClip(clipData)
}