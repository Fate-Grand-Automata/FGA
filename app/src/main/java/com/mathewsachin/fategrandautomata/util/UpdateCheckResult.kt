package com.mathewsachin.fategrandautomata.util

sealed class UpdateCheckResult {
    data class Available(val version: String) : UpdateCheckResult()
    object NotAvailable : UpdateCheckResult()
    data class Failed(val e: Exception) : UpdateCheckResult()
}