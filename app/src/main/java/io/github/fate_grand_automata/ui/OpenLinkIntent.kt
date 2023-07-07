package io.github.fate_grand_automata.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openLinkIntent(context: Context, linkResource: Int) =
    openLinkIntent(context, context.getString(linkResource))

fun openLinkIntent(context: Context, link: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(link)
    )
    context.startActivity(intent)
}