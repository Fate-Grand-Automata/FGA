package com.mathewsachin.fategrandautomata.ui

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.*

@Composable
fun BackHandler(
    backDispatcher: OnBackPressedDispatcher,
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }

    SideEffect {
        backCallback.isEnabled = enabled
    }

    DisposableEffect(backDispatcher) {
        backDispatcher.addCallback(backCallback)

        onDispose {
            backCallback.remove()
        }
    }
}