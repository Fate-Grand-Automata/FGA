package com.mathewsachin.fategrandautomata.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.mathewsachin.fategrandautomata.R
import java.io.PrintWriter
import java.io.StringWriter

val Exception.messageAndStackTrace get (): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    return "${message}\n\n${sw}"
}

fun ClipboardManager.set(context: Context, e: Exception) {
    val clipData = ClipData.newPlainText(
        context.getString(R.string.unexpected_error),
        e.messageAndStackTrace
    )

    setPrimaryClip(clipData)
}