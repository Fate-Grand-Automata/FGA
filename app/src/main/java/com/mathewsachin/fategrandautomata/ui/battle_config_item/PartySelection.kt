package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.animation.core.animateFloatAsState
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
import com.mathewsachin.fategrandautomata.ui.ThemedDialog
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.milliseconds

@Composable
fun PartySelection(config: BattleConfigCore) {
    var party by config.party.remember()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ThemedDialog(
            onDismiss = { showDialog = false }
        ) {
            PartySelectionDialogContent(
                party = party,
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
    party: Int,
    onSelectedChange: (Int) -> Unit
) {
    var selected by remember(party) { mutableStateOf(party) }

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

        var enabled by remember { mutableStateOf(false) }
        val count = 10
        val theta = (2 * Math.PI / count).toFloat()
        val baseStartAngle = -(Math.PI / 2).toFloat()
        var startAngle by remember { mutableStateOf(baseStartAngle - (selected.coerceAtLeast(0) + 1) * theta) }

        val scope = rememberCoroutineScope()

        LaunchedEffect(selected) {
            scope.launch {
                startAngle = baseStartAngle - selected.coerceAtLeast(0) * theta
                delay(100.milliseconds)
                enabled = true
            }
        }

        fun onChange(value: Int) {
            scope.launch {
                enabled = false
                selected = value
                delay(500.milliseconds)
                onSelectedChange(value)
                enabled = true
            }
        }

        val startAngleAnimated by animateFloatAsState(startAngle)

        Layout(
            content = {
                repeat(count) {
                    Card(
                        elevation = 10.dp,
                        shape = CircleShape,
                        backgroundColor = if (selected == it)
                            MaterialTheme.colors.secondary
                        else MaterialTheme.colors.surface
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clickable(
                                    onClick = { onChange(it) },
                                    enabled = enabled
                                )
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
                var angle = startAngleAnimated

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
                    .clickable(
                        onClick = { onChange(-1) },
                        enabled = enabled
                    )
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