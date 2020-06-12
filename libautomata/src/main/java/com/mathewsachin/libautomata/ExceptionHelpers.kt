package com.mathewsachin.libautomata

import java.io.PrintWriter
import java.io.StringWriter

val Exception.messageAndStackTrace get (): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    return "${message}\n\n${sw}"
}