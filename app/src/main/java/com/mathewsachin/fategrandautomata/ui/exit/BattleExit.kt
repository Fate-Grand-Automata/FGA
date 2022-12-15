package com.mathewsachin.fategrandautomata.ui.exit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.*
import com.mathewsachin.fategrandautomata.ui.battle_config_item.Material
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.util.KnownException
import com.mathewsachin.fategrandautomata.util.stringRes
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
        e.let {
            if (it is KnownException) it.reason.msg
            else "${stringResource(R.string.unexpected_error)}: ${e.message}"
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
    is AutoBattle.ExitReason.SkillCommandParseError -> "AutoSkill Parse error:\n\n${e.message}"
    is AutoBattle.ExitReason.CardPriorityParseError -> msg
    AutoBattle.ExitReason.FirstClearRewards -> stringResource(R.string.first_clear_rewards)
    AutoBattle.ExitReason.Paused -> stringResource(R.string.script_paused)
    AutoBattle.ExitReason.StopAfterThisRun -> stringResource(R.string.stop_after_this_run)
}

@Composable
private fun Refill(
    limit: Int,
    timesRefilled: Int
) {
    if (limit > 0) {
        SmallChip(
            text = "$timesRefilled / $limit",
            icon = icon(R.drawable.ic_apple)
        )
    }
}

private fun LazyListScope.battleExitContent(
    reason: AutoBattle.ExitReason,
    state: AutoBattle.ExitState,
    refillEnabled: Boolean
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
                    timesRefilled = state.timesRefilled
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

@Composable
private fun MaterialSummary(
    materials: Map<MaterialEnum, Int>
) {
    LazyRow(
        contentPadding = PaddingValues(16.dp)
    ) {
        items(materials.toList()) { (mat, count) ->
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = 5.dp)
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
    text: String,
    icon: VectorIcon? = null
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
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .size(16.dp)
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
                    refillEnabled = prefs.refill.resources.isNotEmpty()
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
                        reason is AutoBattle.ExitReason.Unexpected && reason.e !is KnownException
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

@Preview
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
//                        MaterialEnum.Chain to 5
//                    ),
                    withdrawCount = 1,
                    totalTime = 1880.seconds,
                    averageTimePerRun = 75.seconds,
                    minTurnsPerRun = 3,
                    maxTurnsPerRun = 4,
                    averageTurnsPerRun = 6
                ),
                refillEnabled = true
            )
        }
    }
}