package com.mathewsachin.fategrandautomata.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.platform.AmbientLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun PreventRtl(content: @Composable () -> Unit) {
    Providers(AmbientLayoutDirection provides LayoutDirection.Ltr) {
        content()
    }
}