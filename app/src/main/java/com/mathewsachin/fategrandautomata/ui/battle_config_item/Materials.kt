package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.drawable
import com.mathewsachin.fategrandautomata.util.stringRes
import com.mathewsachin.fategrandautomata.util.toggle
import com.vanpra.composematerialdialogs.bottomPadding
import java.util.*

@Composable
fun Pref<Set<MaterialEnum>>.Materials() {
    var selected by remember()
    var showDialog by androidx.compose.runtime.remember { mutableStateOf(false) }

    if (showDialog) {
        ThemedDialog(
            onDismiss = { showDialog = false }
        ) {
            Surface {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.p_mats),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    var current by androidx.compose.runtime.remember(selected) { mutableStateOf(selected) }

                    BoxWithConstraints {
                        val modifier = Modifier
                            .heightIn(max = maxHeight * 0.6f)
                            .then(bottomPadding)

                        MaterialsPickerContent(
                            selected = current,
                            onSelectedChange = { current = it },
                            modifier = modifier
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }

                        TextButton(
                            onClick = {
                                selected = current
                                showDialog = false
                            }
                        ) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(vertical = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_mats)
                .toUpperCase(Locale.ROOT),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(start = 16.dp)
        )

        MaterialsSummary(materials = selected.toList())
    }
}

@Composable
fun Material(mat: MaterialEnum) {
    Image(
        painterResource(mat.drawable),
        contentDescription = stringResource(mat.stringRes),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(3.dp)
            .size(20.dp)
            .clip(CircleShape)
            .border(0.5.dp, MaterialTheme.colors.onSurface, CircleShape)
            .alpha(0.8f)
    )
}

@Composable
fun MaterialsSummary(materials: List<MaterialEnum>) {
    if (materials.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, top = 5.dp, bottom = 5.dp)
        ) {
            items(materials) { mat ->
                Material(mat)
            }
        }
    }
    else {
        Text(
            "--",
            modifier = Modifier.padding(16.dp, 5.dp)
        )
    }
}

@Composable
fun MaterialsPickerContent(
    selected: Set<MaterialEnum>,
    onSelectedChange: (Set<MaterialEnum>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 0.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        items(MaterialEnum.values()) { mat ->
            val isSelected = mat in selected

            Card(
                shape = CircleShape,
                backgroundColor =
                    if (isSelected)
                        MaterialTheme.colors.secondary
                    else MaterialTheme.colors.surface,
                contentColor =
                    if (isSelected)
                        MaterialTheme.colors.onSecondary
                    else MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            onSelectedChange(selected.toggle(mat))
                        }
                        .padding(16.dp, 5.dp)
                ) {
                    Material(mat)

                    Text(
                        stringResource(mat.stringRes),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    )

                    if (isSelected) {
                        Icon(
                            rememberVectorPainter(Icons.Default.Check),
                            contentDescription = "check"
                        )
                    }
                }
            }
        }
    }
}