package com.mathewsachin.fategrandautomata.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Fragment.nav(action: NavDirections) {
    try {
        findNavController().navigate(action)
    } catch (e: Exception) {
        // Navigation errors can happen if user clicks on 2 destinations at the same time
        logger.error("Nav Error", e)
    }
}