package io.github.fate_grand_automata.ui.support_img_namer

import android.content.Context
import android.content.DialogInterface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.padding
import io.github.fate_grand_automata.ui.scrollbar
import io.github.fate_grand_automata.util.FakedComposeView
import io.github.fate_grand_automata.util.StorageProvider
import io.github.fate_grand_automata.util.showOverlayDialog
import io.github.fate_grand_automata.util.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume


suspend fun showSupportImageMaker(
    context: Context,
    storageProvider: StorageProvider
) = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        var dialog: DialogInterface? = null

        val composeView = FakedComposeView(context) {
            SupportImageMakerScreen(
                storageProvider = storageProvider,
                onResponse = {
                    dialog?.dismiss()
                }
            )
        }
        dialog = showOverlayDialog(context) {
            setView(composeView.view)

            setOnDismissListener {
                composeView.close()
                continuation.resume(Unit)
            }
        }
    }
}

@Composable
private fun SupportImageMakerScreen(
    storageProvider: StorageProvider,
    onResponse: () -> Unit
) {
    val entries by remember {
        derivedStateOf {
            getSupportEntries(storageProvider)
        }
    }

    val state = rememberLazyListState()
    FGATheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.p_script_mode_support_image_maker),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 5.dp)
            )
            HorizontalDivider()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds(),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .scrollbar(
                            state = state,
                            horizontal = false,
                            knobColor = MaterialTheme.colorScheme.secondary,
                            hiddenAlpha = 0.3f
                        ),
                    contentPadding = PaddingValues(
                        vertical = 8.dp
                    ),
                    state = state
                ) {
                    entries.forEach { support ->
                        if (support.index == 0) {
                            item {
                                Text(
                                    text = stringResource(id = support.kind.title),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(
                                        horizontal = MaterialTheme.padding.small,
                                    )
                                )
                            }
                            item {
                                HorizontalDivider(
                                    modifier = Modifier.padding(
                                        vertical = MaterialTheme.padding.extraSmall,
                                        horizontal = MaterialTheme.padding.small,
                                    )
                                )
                            }
                        }
                        item {
                            support.Entry()
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.padding.small,
                )
            ) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        onResponse()
                    }
                ) {
                    Text(
                        text = stringResource(android.R.string.cancel).uppercase()
                    )
                }
                Spacer(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.padding.extraSmall)
                )
                TextButton(
                    onClick = {
                        entries
                            .filter { item ->
                                item.isValid()
                            }
                            .forEach { item ->
                                Timber.d("Renaming ${item.kind} ${item.index}")
                                item.rename(storageProvider)
                            }

                        onResponse()
                    },
                    enabled = entries
                        .filter { it.checkBoxStatus }
                        .any { it.isValid() }
                ) {
                    Text(
                        text = stringResource(R.string.save).uppercase()
                    )
                }
            }
        }
    }
}


