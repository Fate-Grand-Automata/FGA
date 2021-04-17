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
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.ui.FgaDialog
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PartySelection(config: BattleConfigCore) {
    var party by config.party.remember()

    val dialog = FgaDialog()

    dialog.build(
        color = MaterialTheme.colors.background
    ) {
        title(stringResource(R.string.p_battle_config_party))

        var currentParty by remember(party) { mutableStateOf(party) }

        PartySelectionDialogContent(
            selected = currentParty,
            onSelectedChange = { currentParty = it }
        )

        buttons(
            onSubmit = { party = currentParty }
        )
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
                .clickable(onClick = { dialog.show() })
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

        val count = 10
        val theta = (2 * Math.PI / count).toFloat()
        val startAngle = -(Math.PI / 2).toFloat()

        Layout(
            content = {
                repeat(count) {
                    Card(
                        elevation = 10.dp,
                        shape = CircleShape,
                        backgroundColor = if (selected == it)
                            MaterialTheme.colors.secondary
                        else MaterialTheme.colors.surface,
                        contentColor = if (selected == it)
                            MaterialTheme.colors.onSecondary
                        else MaterialTheme.colors.onSurface
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { onSelectedChange(it) }
                        ) {
                            Text("${it + 1}")
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

            val squareSide = minOf(constraints.maxWidth, constraints.maxHeight)
            val center = squareSide / 2f
            val radius = squareSide * 0.35

            layout(squareSide, squareSide) {
                var angle = startAngle

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
            backgroundColor = if (selected == -1)
                MaterialTheme.colors.secondary
            else MaterialTheme.colors.surface,
            contentColor = if (selected == -1)
                MaterialTheme.colors.onSecondary
            else MaterialTheme.colors.onSurface,
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clickable { onSelectedChange(-1) }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.p_not_set)
                )
            }
        }
    }
}