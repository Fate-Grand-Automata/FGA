package com.mathewsachin.fategrandautomata.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
private fun OnLifecycle(predicate: (Lifecycle.Event) -> Boolean, callback: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val callbackState by rememberUpdatedState(callback)

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (predicate(event)) {
                callbackState()
            }
        }

        lifecycle.addObserver(observer)

        onDispose { lifecycle.removeObserver(observer) }
    }
}

@Composable
fun OnResume(callback: () -> Unit) {
    OnLifecycle(
        predicate = { it.targetState == Lifecycle.State.RESUMED },
        callback = callback
    )
}

@Composable
fun OnPause(callback: () -> Unit) {
    OnLifecycle(
        predicate = { it == Lifecycle.Event.ON_PAUSE },
        callback = callback
    )
}