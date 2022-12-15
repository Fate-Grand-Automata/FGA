package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.multiChoiceList
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.drawable
import com.mathewsachin.fategrandautomata.util.stringRes

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
                    .alignByBaseline()
            ) {
                title(stringResource(R.string.p_mats))
            }

            TextButton(
                onClick = { current = emptySet() },
                modifier = Modifier
                    .padding(16.dp, 5.dp)
                    .alignByBaseline()
            ) {
                // TODO: Localize
                Text("CLEAR")
            }
        }

        multiChoiceList(
            selected = current,
            onSelectedChange = { current = it },
            items = MaterialEnum.values().toList()
        ) { mat ->
            Material(mat)

            Text(
                stringResource(mat.stringRes),
                modifier = Modifier
                    .padding(start = 16.dp)
            )
        }

        buttons(
            onSubmit = { selected = current }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { dialog.show() }
            .heightIn(min = 70.dp)
            .padding(vertical = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_mats).uppercase(),
            style = MaterialTheme.typography.bodySmall,
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
            .border(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            .alpha(0.8f)
    )
}

@Composable
fun MaterialsSummary(materials: List<MaterialEnum>) {
    if (materials.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, top = 5.dp, bottom = 5.dp),
            modifier = Modifier.height(40.dp)
        ) {
            items(materials) { mat ->
                Material(mat)
            }
        }
    } else {
        Text(
            "--",
            modifier = Modifier.padding(16.dp, 5.dp)
        )
    }
}