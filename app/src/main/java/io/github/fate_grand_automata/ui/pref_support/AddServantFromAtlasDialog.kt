package io.github.fate_grand_automata.ui.pref_support

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.atlas.AtlasServant
import io.github.fate_grand_automata.ui.dialog.ChoiceListItem
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.Preference

/**
 * Adds a servant to the preferred servants by picking one from the Atlas Academy DB
 * (https://apps.atlasacademy.io/db) instead of creating their images in-game.
 */
@Composable
fun AddServantFromAtlasPreference(
    vm: AtlasServantViewModel,
    onAdded: (String) -> Unit
) {
    val dialog = addServantDialog(vm, onAdded)

    Preference(
        icon = icon(R.drawable.ic_image_search),
        title = stringResource(R.string.p_battle_config_support_add_from_db),
        summary = stringResource(R.string.p_battle_config_support_add_from_db_hint),
        onClick = { dialog.show() }
    )
}

@Composable
private fun addServantDialog(
    vm: AtlasServantViewModel,
    onAdded: (String) -> Unit
): FgaDialog {
    val dialog = FgaDialog()
    val context = LocalContext.current

    var selectedId by remember { mutableStateOf<Int?>(null) }
    val selected = vm.results.firstOrNull { it.id == selectedId }

    dialog.build {
        title(text = stringResource(R.string.p_battle_config_support_add_from_db))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = vm.query,
                    onValueChange = vm::updateQuery,
                    label = { Text(stringResource(R.string.atlas_servant_name)) },
                    singleLine = true,
                    enabled = !vm.isAdding,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { vm.search() }),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { vm.search() },
                    enabled = !vm.isSearching && !vm.isAdding
                ) {
                    Icon(
                        rememberVectorPainter(Icons.Default.Search),
                        contentDescription = stringResource(R.string.atlas_servant_search)
                    )
                }
            }

            val error = vm.error
            val message = when {
                vm.isSearching -> stringResource(R.string.atlas_servant_searching)
                error != null -> stringResource(error)
                vm.hasSearched && vm.results.isEmpty() ->
                    stringResource(R.string.atlas_servant_no_results)

                else -> null
            }

            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (vm.isAdding) {
                Text(
                    text = stringResource(R.string.atlas_servant_adding),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        constrained { modifier ->
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 0.dp),
                modifier = modifier.fillMaxWidth()
            ) {
                items(vm.results, key = { it.id }) { servant ->
                    ChoiceListItem(
                        isSelected = servant.id == selectedId,
                        onClick = { selectedId = servant.id }
                    ) {
                        ServantDetails(servant, face = vm.faces[servant.id])
                    }
                }
            }
        }

        /*
         * The dialog stays open while the images download, so a failure can be shown in place
         * instead of behind a dismissed dialog.
         */
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 5.dp)
        ) {
            TextButton(
                onClick = { dialog.hide() },
                enabled = !vm.isAdding
            ) {
                Text(stringResource(android.R.string.cancel).uppercase())
            }

            TextButton(
                onClick = {
                    selected?.let { servant ->
                        vm.add(context, servant) { name ->
                            dialog.hide()
                            onAdded(name)
                        }
                    }
                },
                enabled = selected != null && !vm.isAdding
            ) {
                Text(stringResource(R.string.atlas_servant_add).uppercase())
            }
        }
    }

    return dialog
}

@Composable
private fun ServantDetails(servant: AtlasServant, face: Bitmap?) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServantFace(face)

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(servant.name)

            Text(
                text = stringResource(
                    R.string.atlas_servant_class_rarity,
                    servant.className.replaceFirstChar { it.uppercase() },
                    servant.rarity
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** The servant's face art, or an empty placeholder until it has downloaded. */
@Composable
private fun ServantFace(face: Bitmap?) {
    val faceSize = 44.dp
    val shape = RoundedCornerShape(6.dp)

    if (face != null) {
        val image = remember(face) { face.asImageBitmap() }

        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(faceSize)
                .clip(shape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(faceSize)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}
