package io.github.fate_grand_automata.ui.battle_config_item

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.drawable
import io.github.fate_grand_automata.util.stringRes


@Composable
fun MaterialDisplay(
    modifier: Modifier = Modifier,
    config: BattleConfigCore,
    onNavigate: () -> Unit
){
    val selected by config.materials.remember()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onNavigate
            )
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