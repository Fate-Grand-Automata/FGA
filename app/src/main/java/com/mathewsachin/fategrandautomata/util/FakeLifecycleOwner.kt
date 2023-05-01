package com.mathewsachin.fategrandautomata.util

// From: https://gist.github.com/handstandsam/6ecff2f39da72c0b38c07aa80bbb5a2f

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private class FakeLifecycleOwner : SavedStateRegistryOwner {
    private var lifecycleRegistry = LifecycleRegistry(this)
    private var savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle get() = lifecycleRegistry

    fun setCurrentState(state: Lifecycle.State) {
        lifecycleRegistry.currentState = state
    }

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }

    fun performSave(outBundle: Bundle) {
        savedStateRegistryController.performSave(outBundle)
    }
}

class FakedComposeView(
    private val context: Context,
    private val content: @Composable () -> Unit
) : AutoCloseable {
    private val viewModelStore = ViewModelStore()
    val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore
            get() = viewModelStore
    }
    private val lifecycleOwner = FakeLifecycleOwner()

    private val coroutineContext = AndroidUiDispatcher.CurrentThread
    private val runRecomposeScope = CoroutineScope(coroutineContext)
    private val recomposer = Recomposer(coroutineContext)

    val view
        get() = ComposeView(context).also {
            it.setContent { content() }

            // Trick The ComposeView into thinking we are tracking lifecycle
            lifecycleOwner.performRestore(null)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            it.setViewTreeLifecycleOwner(lifecycleOwner)
            it.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
            it.setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            it.compositionContext = recomposer
            runRecomposeScope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }
        }

    override fun close() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        runRecomposeScope.cancel()
    }
}