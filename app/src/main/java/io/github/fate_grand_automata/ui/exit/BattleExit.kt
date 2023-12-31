package io.github.fate_grand_automata.ui.exit

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.ui.VectorIcon
import io.github.fate_grand_automata.ui.battle_config_item.Material
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.KnownException
import io.github.fate_grand_automata.util.stringRes
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val Duration.stringify: String
    get() =
        toComponents { hours, minutes, seconds, _ ->
            (if (hours > 0)
                listOf(hours, minutes, seconds)
            else listOf(minutes, seconds))
                .joinToString(":") { "%02d".format(it) }
        }

@Composable
private fun AutoBattle.ExitReason.text(): String = when (this) {
    AutoBattle.ExitReason.Abort -> stringResource(R.string.stopped_by_user)
    is AutoBattle.ExitReason.Unexpected -> {
        cause.let {
            if (it is KnownException) it.reason.msg
            else "${stringResource(R.string.unexpected_error)}: ${cause?.message}"
        }
    }

    AutoBattle.ExitReason.CEGet -> stringResource(R.string.ce_get)
    is AutoBattle.ExitReason.LimitCEs -> stringResource(R.string.ces_dropped, count)
    is AutoBattle.ExitReason.LimitMaterials -> stringResource(R.string.mats_farmed, count)
    AutoBattle.ExitReason.WithdrawDisabled -> stringResource(R.string.withdraw_disabled)
    AutoBattle.ExitReason.APRanOut -> stringResource(R.string.script_msg_ap_ran_out)
    AutoBattle.ExitReason.InventoryFull -> stringResource(R.string.inventory_full)
    is AutoBattle.ExitReason.LimitRuns -> stringResource(R.string.times_ran, count)
    AutoBattle.ExitReason.SupportSelectionManual -> stringResource(R.string.support_selection_manual)
    AutoBattle.ExitReason.SupportSelectionFriendNotSet -> stringResource(R.string.support_selection_friend_not_set)
    AutoBattle.ExitReason.SupportSelectionPreferredNotSet -> stringResource(R.string.support_selection_preferred_not_set)
    is AutoBattle.ExitReason.SkillCommandParseError -> "AutoSkill Parse error:\n\n${cause?.message}"
    is AutoBattle.ExitReason.CardPriorityParseError -> msg
    AutoBattle.ExitReason.FirstClearRewards -> stringResource(R.string.first_clear_rewards)
    AutoBattle.ExitReason.Paused -> stringResource(R.string.script_paused)
    AutoBattle.ExitReason.StopAfterThisRun -> stringResource(R.string.stop_after_this_run)
    AutoBattle.ExitReason.StormPodRanOut -> stringResource(R.string.script_msg_storm_pods_ran_out)
}

@Composable
private fun Refill(
    limit: Int,
    timesRefilled: Int,
    selectedApple: RefillResourceEnum
) {
    if (limit > 0) {
        SmallChip(
            text = "$timesRefilled / $limit",
            icon = icon(R.drawable.ic_apple),
            tint = when(selectedApple){
                RefillResourceEnum.SQ -> null
                else -> selectedApple.color
            },
            iconModifier =  when(selectedApple){
                RefillResourceEnum.SQ -> Modifier
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        val rainbowAppleColorList = listOf(
                            Color(0xFFe71d43),
                            Color(0xFFff3700),
                            Color(0xFFffa500),
                            Color(0xFFaad500),
                            Color(0xFF002baa),
                            Color(0xFF3200ac),
                            Color(0xFF812ba6)
                        )
                        val brush = Brush.linearGradient(rainbowAppleColorList)
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcAtop)
                        }
                    }
                else -> Modifier
            }
        )
    }
}

val RefillResourceEnum.color: Color
    get() = when (this) {
        RefillResourceEnum.SQ -> Color(0xFFe99fa2)
        RefillResourceEnum.Gold -> Color(0xFFe9b717)
        RefillResourceEnum.Silver -> Color(0xFFb0b0b0)
        RefillResourceEnum.Bronze -> Color(0xFF4bc8e7)
        RefillResourceEnum.Copper -> Color(0xFFac9283)
    }

private fun LazyListScope.battleExitContent(
    reason: AutoBattle.ExitReason,
    state: AutoBattle.ExitState,
    refillEnabled: Boolean,
    selectedApple: RefillResourceEnum
) {
    item {
        Text(
            reason.text(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp, top = 5.dp)
        )
    }

    item {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp, 5.dp)
        ) {
            if (refillEnabled) {
                Refill(
                    limit = state.refillLimit,
                    timesRefilled = state.timesRefilled,
                    selectedApple = selectedApple,
                )
            }

            SmallChip(
                text = state.totalTime.stringify,
                icon = icon(R.drawable.ic_time)
            )

            Runs(
                runLimit = state.runLimit,
                timesRan = state.timesRan
            )
        }
    }

    if (reason !is AutoBattle.ExitReason.LimitCEs && state.ceDropCount > 0) {
        item {
            Text(
                stringResource(R.string.ces_dropped, state.ceDropCount),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(16.dp, 5.dp)
            )
        }
    }

    if (state.timesRan > 1) {
        item {
            Text(
                stringResource(
                    R.string.avg_time_per_run,
                    state.averageTimePerRun.stringify
                ),
                modifier = Modifier
                    .padding(16.dp, 5.dp)
            )
        }

        item {
            Text(
                stringResource(
                    R.string.turns_stats,
                    state.minTurnsPerRun,
                    state.averageTurnsPerRun,
                    state.maxTurnsPerRun
                ),
                modifier = Modifier
                    .padding(16.dp, 5.dp)
            )
        }
    } else if (state.timesRan == 1) {
        item {
            Text(
                stringResource(R.string.turns_count, state.minTurnsPerRun),
                modifier = Modifier
                    .padding(16.dp, 5.dp)
            )
        }
    }

    if (state.materials.isNotEmpty()) {
        item {
            MaterialSummary(
                materials = state.materials
            )
        }
    }

    if (state.withdrawCount > 0) {
        item {
            Text(
                stringResource(R.string.times_withdrew, state.withdrawCount),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(16.dp, 5.dp)
            )
        }
    }
}

@Composable
private fun Runs(
    runLimit: Int?,
    timesRan: Int
) {
    val runs = when {
        runLimit != null && runLimit > 0 -> "$timesRan / $runLimit"
        timesRan > 0 -> timesRan.toString()
        else -> ""
    }

    if (runs.isNotBlank()) {
        SmallChip(
            text = "${stringResource(R.string.p_runs)}: $runs"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialSummary(
    materials: Map<MaterialEnum, Int>
) {
    FlowRow(
        maxItemsInEachRow = 3,
        modifier = Modifier.padding(16.dp)
    ) {
        for ((mat, count) in materials.toList()) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 5.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp, 5.dp)
                ) {
                    Material(mat = mat)

                    Text(
                        stringResource(mat.stringRes),
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                    )

                    Text(
                        "x$count",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallChip(
    iconModifier: Modifier = Modifier,
    text: String,
    icon: VectorIcon? = null,
    tint: Color? = null
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(end = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(5.dp, 3.dp)
        ) {
            if (icon != null) {
                DimmedIcon(
                    icon,
                    contentDescription = "icon",
                    modifier = iconModifier
                        .padding(end = 5.dp)
                        .size(16.dp),
                    tint = tint
                )
            }

            Text(
                text,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun BattleExit(
    exception: AutoBattle.ExitException,
    prefs: IPreferences,
    prefsCore: PrefsCore,
    onClose: () -> Unit,
    onCopy: () -> Unit
) {
    FgaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                battleExitContent(
                    reason = exception.reason,
                    state = exception.state,
                    refillEnabled = prefs.selectedServerConfigPref.resources.isNotEmpty(),
                    selectedApple = prefs.selectedServerConfigPref.selectedApple
                )
            }

            if (exception.reason is AutoBattle.ExitReason.Paused) {
                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.stop_after_this_run),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    var stopAfterThisRun by prefsCore.stopAfterThisRun.remember()

                    Switch(
                        checked = stopAfterThisRun,
                        onCheckedChange = { stopAfterThisRun = true }
                    )
                }
            }

            Divider()

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    val allowCopy = exception.reason.let { reason ->
                        reason is AutoBattle.ExitReason.Unexpected && reason.cause !is KnownException
                    }

                    if (allowCopy) {
                        TextButton(onClick = onCopy) {
                            Text(stringResource(R.string.unexpected_error_copy))
                        }
                    }
                }

                Row {
                    TextButton(
                        onClick = onClose
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", widthDp = 600, heightDp = 300)
@Preview(name = "Dark Mode", widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewBattleExitContent() {
    FGATheme {
        LazyColumn {
            battleExitContent(
                reason = AutoBattle.ExitReason.CEGet,
                state = AutoBattle.ExitState(
                    timesRan = 5,
                    runLimit = 8,
                    timesRefilled = 3,
                    refillLimit = 6,
                    ceDropCount = 2,
                    materials = emptyMap(),
//                    materials = mapOf(
//                        MaterialEnum.ShellOfReminiscence to 2,
//                        MaterialEnum.Chain to 5,
//                        MaterialEnum.AmnestyBell to 1,
//                        MaterialEnum.AuroraSteel to 10
//                    ),
                    withdrawCount = 1,
                    totalTime = 1880.seconds,
                    averageTimePerRun = 75.seconds,
                    minTurnsPerRun = 3,
                    maxTurnsPerRun = 4,
                    averageTurnsPerRun = 3.45678
                ),
                refillEnabled = true,
                selectedApple = RefillResourceEnum.Copper
            )
        }
    }
}