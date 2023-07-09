package io.github.fate_grand_automata.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openLinkIntent(linkResource: Int) =
    this.openLinkIntent(getString(linkResource))

fun Context.openLinkIntent(link: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(link)
    )
    startActivity(intent)
}