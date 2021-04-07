package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ThemedDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes

    Dialog(onDismissRequest = onDismiss) {
        MaterialTheme(
            colors = colors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

@Composable
fun PartySelection(config: BattleConfigCore) {
    var party by config.party.remember()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ThemedDialog(
            onDismiss = { showDialog = false }
        ) {
            PartySelectionDialogContent(
                selected = party,
                onSelectedChange = {
                    party = it
                    showDialog = false
                }
            )
        }
    }

    Card(
        elevation = 3.dp,
        shape = CircleShape,
        modifier = Modifier
            .padding(vertical = 5.dp)
            .padding(end = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { showDialog = true }
                .padding(16.dp, 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.p_battle_config_party)
                    .toUpperCase(Locale.ROOT),
                style = MaterialTheme.typography.caption
            )

            Text(
                if (party == -1) "--" else (party + 1).toString(),
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
fun PartySelectionDialogContent(
    selected: Int,
    onSelectedChange: (Int) -> Unit
) {
    Box {
        Surface(
            color = MaterialTheme.colors.primary,
            shape = CircleShape,
            modifier = Modifier
                .padding(40.dp)
        ) {
            Layout(
                content = {}
            ) { _, constraints ->
                val squareSide = minOf(constraints.maxWidth, constraints.maxHeight)

                layout(squareSide, squareSide) { }
            }
        }

        Layout(
            content = {
                (1..10).forEach {
                    Card(
                        elevation = 10.dp,
                        shape = CircleShape,
                        backgroundColor = if (selected == it - 1)
                            MaterialTheme.colors.secondary
                        else MaterialTheme.colors.surface
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable(onClick = { onSelectedChange(it - 1) })
                        ) {
                            Text(it.toString())
                        }
                    }
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                measurable.measure(
                    constraints.copy(minHeight = 0, minWidth = 0)
                )
            }

            val theta = 2 * Math.PI / measurables.size
            val squareSide = minOf(constraints.maxWidth, constraints.maxHeight)
            val center = squareSide / 2f
            val radius = squareSide * 0.35

            layout(squareSide, squareSide) {
                var angle = -2 * theta

                placeables.forEach { placeable ->
                    placeable.place(
                        x = (center + radius * cos(angle) - placeable.width / 2).roundToInt(),
                        y = (center + radius * sin(angle) - placeable.height / 2).roundToInt()
                    )

                    angle += theta
                }
            }
        }

        Card(
            elevation = 10.dp,
            shape = CircleShape,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clickable(onClick = { onSelectedChange(-1) })
            ) {
                Icon(
                    Icons.Default.Close,
                    tint = MaterialTheme.colors.primary,
                    contentDescription = stringResource(R.string.p_not_set)
                )
            }
        }

        Text(
            stringResource(R.string.p_battle_config_party)
                .toUpperCase(Locale.ROOT),
            color = MaterialTheme.colors.onPrimary,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        )
    }
}