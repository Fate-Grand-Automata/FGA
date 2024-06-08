package io.github.fate_grand_automata.ui.exit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.entrypoints.AutoAppend
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.util.KnownException


@Composable
fun AppendExit(
    exception: AutoAppend.ExitException,
    prefs: IPreferences,
    onClose: () -> Unit,
    onCopy: () -> Unit
) {
    LaunchedEffect(key1 = Unit, block = {
        prefs.append.appendOneLocked = false
        prefs.append.appendTwoLocked = false
        prefs.append.appendThreeLocked = false

        prefs.append.shouldUnlockAppendOne = false
        prefs.append.shouldUnlockAppendTwo = false
        prefs.append.shouldUnlockAppendThree = false

        prefs.append.upgradeAppendOne = 0
        prefs.append.upgradeAppendTwo = 0
        prefs.append.upgradeAppendThree = 0

    })

    FgaScreen {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppendExitContent(reason = exception.reason, state = exception.state)
            }

            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    val allowCopy = exception.reason.let { reason ->
                        reason is AutoAppend.ExitReason.Unexpected && reason.e !is KnownException
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

@Composable
private fun AppendExitContent(
    reason: AutoAppend.ExitReason,
    state: AutoAppend.ExitState
) {
    Text(
        text = reason.text().uppercase(),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 5.dp)
    )

    val items = state.appendSummaryList
    Row(
        modifier = Modifier
    ) {
        items.forEachIndexed { index, summary ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(id = R.string.append_number, index + 1).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )
                AppendSummary(reason = reason, summary = summary)
            }
        }
    }
}

@Composable
private fun AppendSummary(
    reason: AutoAppend.ExitReason,
    summary: AutoAppend.Summary,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (reason == AutoAppend.ExitReason.Abort && (summary.upgradeLevel > 0 || summary.shouldUnlock)) {
            item {
                Text(
                    text = stringResource(id = R.string.enhancement_halt_aborted).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        summary.numberOfUpgradePerform?.let { num ->
            if (num > 0){
                item {
                    Text(
                        text = stringResource(id = R.string.upgraded_number_of_times, num).uppercase(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (summary.upgradeLevel != num) {
                item {
                    Text(
                        text = stringResource(id = R.string.append_unable_to_upgrade_n_times,
                            summary.upgradeLevel).uppercase(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        summary.upgradeResult?.let {
            item {
                Text(
                    text = it.reason.text().uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } ?: run {
            item {
                Text(
                    text = stringResource(id = R.string.enhancement_error_no_enhancement).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun AutoAppend.ExitReason.text(): String = when (this) {
    AutoAppend.ExitReason.Abort -> stringResource(R.string.stopped_by_user)
    AutoAppend.ExitReason.Done -> stringResource(id = R.string.done)
    AutoAppend.ExitReason.NoServantSelected -> stringResource(id = R.string.enhancement_missing_servant)
    AutoAppend.ExitReason.RanOutOfQP -> stringResource(id = R.string.ran_out_of_qp)
    is AutoAppend.ExitReason.Unexpected -> {
        e.let {
            if (it is KnownException) it.reason.msg
            else "${stringResource(R.string.unexpected_error)}: ${e.message}"
        }
    }
}

@Composable
private fun AutoAppend.EnhancementExitReason.text(): String = when (this) {
    AutoAppend.EnhancementExitReason.Success -> stringResource(id = R.string.success)
    AutoAppend.EnhancementExitReason.ExitEarlyOutOfQPException ->
        stringResource(id = R.string.enhancement_error_exit_early_out_of_qp)

    AutoAppend.EnhancementExitReason.NotSelected -> stringResource(id = R.string.enhancement_error_no_enhancement)
    AutoAppend.EnhancementExitReason.RanOutOfMats -> stringResource(id = R.string.enhancement_error_out_of_mats)
    AutoAppend.EnhancementExitReason.RanOutOfQP -> stringResource(id = R.string.enhancement_error_out_of_qp)
    AutoAppend.EnhancementExitReason.UnableToUnlock -> stringResource(id = R.string.append_unable_to_unlock)
    AutoAppend.EnhancementExitReason.UnlockSuccess -> stringResource(id = R.string.append_unlock_success)
    AutoAppend.EnhancementExitReason.UnableToUpgradeFurther ->
        stringResource(id = R.string.append_unable_to_upgrade_further)
    AutoAppend.EnhancementExitReason.Lag -> stringResource(id = R.string.append_lag)
}