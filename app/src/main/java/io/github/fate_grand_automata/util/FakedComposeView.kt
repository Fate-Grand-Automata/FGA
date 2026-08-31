package io.github.fate_grand_automata.util

// Originally from: https://gist.github.com/handstandsam/6ecff2f39da72c0b38c07aa80bbb5a2f
// Since reworked: modern Compose builds its own window Recomposer, so the manual one the
// gist needed is gone.

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Hosts Compose content in a window with no Activity behind it - the script overlay and the
 * dialogs raised from `ScriptRunnerService` - by standing in as the three ViewTree owners
 * that [ComposeView] looks for when it attaches.
 */
class FakedComposeView(
    context: Context,
    content: @Composable () -> Unit
) : AutoCloseable, LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore = ViewModelStore()
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    val view: ComposeView

    init {
        savedStateRegistryController.performRestore(null)

        // Has to reach at least STARTED: Compose's window Recomposer keeps its frame clock
        // paused below that (ON_START resumes it), and the content would never recompose.
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        // The owners must be in place before the view attaches, which is when ComposeView
        // resolves them out of the view tree.
        view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@FakedComposeView)
            setViewTreeViewModelStoreOwner(this@FakedComposeView)
            setViewTreeSavedStateRegistryOwner(this@FakedComposeView)
            setContent(content)
        }
    }

    override fun close() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}
