package io.github.fate_grand_automata.ui.pref_support

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.atlas.AtlasServant
import io.github.fate_grand_automata.atlas.AtlasSupportServantRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/** Backs the dialog that adds support servants straight from the Atlas Academy DB. */
@HiltViewModel
class AtlasServantViewModel @Inject constructor(
    private val repository: AtlasSupportServantRepository
) : ViewModel() {
    var query by mutableStateOf("")
        private set

    var results by mutableStateOf<List<AtlasServant>>(emptyList())
        private set

    /**
     * Faces of the search results, by servant id, loaded one by one so the list is usable before
     * every image arrived. They are the final ascension's art, the one the DB lists last.
     */
    var faces by mutableStateOf<Map<Int, Bitmap>>(emptyMap())
        private set

    var isSearching by mutableStateOf(false)
        private set

    var isAdding by mutableStateOf(false)
        private set

    var hasSearched by mutableStateOf(false)
        private set

    /** The string resource of the last failure, shown inside the dialog. */
    var error by mutableStateOf<Int?>(null)
        private set

    private var faceJob: Job? = null

    fun updateQuery(value: String) {
        query = value
    }

    fun search() {
        if (isSearching || query.isBlank()) return

        viewModelScope.launch {
            isSearching = true
            error = null

            try {
                results = repository.search(query)
                hasSearched = true

                loadFaces()
            } catch (e: Exception) {
                results = emptyList()
                hasSearched = false
                error = R.string.atlas_servant_search_failed

                Timber.e(e, "Searching Atlas Academy for '$query' failed")
            } finally {
                isSearching = false
            }
        }
    }

    /**
     * Downloads and saves [servant]'s support images, then hands the name it is stored under to
     * [onAdded]. Failures stay in the dialog, so it isn't dismissed until the images are saved.
     */
    fun add(context: Context, servant: AtlasServant, onAdded: (String) -> Unit) {
        if (isAdding) return

        viewModelScope.launch {
            isAdding = true
            error = null

            try {
                val name = repository.add(servant)

                onAdded(name)

                Toast.makeText(
                    context,
                    context.getString(R.string.atlas_servant_added, name),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                error = R.string.atlas_servant_add_failed

                Timber.e(e, "Adding ${servant.name} from Atlas Academy failed")
            } finally {
                isAdding = false
            }
        }
    }

    private fun loadFaces() {
        faceJob?.cancel()

        faceJob = viewModelScope.launch {
            for (servant in results) {
                if (faces.containsKey(servant.id)) continue

                val face = try {
                    repository.face(servant)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.w(e, "Couldn't load the face of ${servant.name}")

                    null
                }

                if (face != null) faces = faces + (servant.id to face)
            }
        }
    }
}
