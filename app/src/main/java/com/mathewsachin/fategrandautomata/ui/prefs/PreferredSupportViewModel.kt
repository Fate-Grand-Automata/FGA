package com.mathewsachin.fategrandautomata.ui.prefs

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewsachin.fategrandautomata.IStorageProvider
import com.mathewsachin.fategrandautomata.SupportImageKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import timber.log.error
import java.io.File

class PreferredSupportViewModel @ViewModelInject constructor(
    val storageProvider: IStorageProvider
): ViewModel() {
    var servants: Map<String, String> by mutableStateOf(emptyMap())
    var ces: Map<String, String> by mutableStateOf(emptyMap())
    var friends: Map<String, String> by mutableStateOf(emptyMap())

    private suspend fun getSupportImages(
        context: Context,
        kind: SupportImageKind
    ) = try {
        withContext(Dispatchers.IO) {
            storageProvider.list(kind)
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
        }
    } catch (e: Exception) {
        val msg = "Couldn't access Support images ($kind)"

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        Timber.error(e) { msg }

        emptyList()
    }

    fun refresh(context: Context) {
        viewModelScope.launch {
            servants =
                getSupportImages(context, SupportImageKind.Servant)
                    .associateWith { it }

            ces =
                getSupportImages(context, SupportImageKind.CE)
                    .associateWith { File(it).nameWithoutExtension }

            friends =
                getSupportImages(context, SupportImageKind.Friend)
                    .associateWith { File(it).nameWithoutExtension }
        }
    }
}