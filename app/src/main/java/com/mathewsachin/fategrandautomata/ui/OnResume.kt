package com.mathewsachin.fategrandautomata.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun OnResume(callback: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val callbackState by rememberUpdatedState(callback)

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event.targetState == Lifecycle.State.RESUMED) {
                callbackState()
            }
        }

        lifecycle.addObserver(observer)

        onDispose { lifecycle.removeObserver(observer) }
    }
}