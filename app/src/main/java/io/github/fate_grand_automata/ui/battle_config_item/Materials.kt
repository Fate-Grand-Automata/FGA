package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.Pref
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.dialog.multiChoiceList
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.drawable
import io.github.fate_grand_automata.util.stringRes

@Composable
fun Pref<Set<MaterialEnum>>.Materials() {
    var selected by remember()

    val dialog = FgaDialog()

    dialog.build {
        var current by remember(selected) { mutableStateOf(selected) }

        Row {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
            ) {
                title(stringResource(R.string.p_mats))
            }

            TextButton(
                onClick = { current = emptySet() },
                modifier = Modifier
                    .padding(16.dp, 5.dp)
                    .alignByBaseline(),
                enabled = current.isNotEmpty(),
            ) {
                // TODO: Localize
                Text("CLEAR")
            }
        }

        multiChoiceList(
            selected = current,
            onSelectedChange = { current = it },
            prioritySelected = true,
            items = MaterialEnum.entries.toList(),
        ) { mat ->
            Material(mat)

            Text(
                stringResource(mat.stringRes),
                modifier = Modifier
                    .padding(start = 16.dp),
            )
        }

        buttons(
            onSubmit = { selected = current },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { dialog.show() }
            .heightIn(min = 55.dp)
            .padding(vertical = 5.dp),
    ) {
        Text(
            stringResource(R.string.p_mats).uppercase(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp),
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
            .border(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            .alpha(0.8f),
    )
}

@Composable
fun MaterialsSummary(materials: List<MaterialEnum>) {
    if (materials.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, top = 5.dp, bottom = 5.dp),
            modifier = Modifier.height(40.dp),
        ) {
            items(materials) { mat ->
                Material(mat)
            }
        }
    } else {
        Text(
            "--",
            modifier = Modifier.padding(16.dp, 5.dp),
        )
    }
}
