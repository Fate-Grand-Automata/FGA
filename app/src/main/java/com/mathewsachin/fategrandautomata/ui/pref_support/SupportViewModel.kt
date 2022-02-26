package com.mathewsachin.fategrandautomata.ui.pref_support

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.SupportImageExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    val storageProvider: StorageProvider
) : ViewModel() {
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
        Timber.e(e, msg)

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

    suspend fun extract(context: Context) {
        SupportImageExtractor(context, storageProvider).extract()

        refresh(context)
    }

    val shouldExtractSupportImages
        get() =
            storageProvider.shouldExtractSupportImages

    suspend fun performSupportImageExtraction(context: Context) {
        val msg = try {
            extract(context)

            context.getString(R.string.support_imgs_extracted)
        } catch (e: Exception) {
            context.getString(R.string.support_imgs_extract_failed).also { msg ->
                Timber.e(e, msg)
            }
        }

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}