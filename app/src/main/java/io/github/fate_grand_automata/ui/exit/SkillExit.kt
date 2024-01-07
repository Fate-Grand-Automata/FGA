package io.github.fate_grand_automata.ui.exit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Divider
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
import io.github.fate_grand_automata.scripts.entrypoints.AutoSkill
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.util.KnownException


@Composable
fun SkillExit(
    exception: AutoSkill.ExitException,
    prefs: IPreferences,
    onClose: () -> Unit,
    onCopy: () -> Unit
) {
    LaunchedEffect(key1 = Unit, block = {
        prefs.skill.shouldUpgradeSkillOne = false
        prefs.skill.skillOneUpgradeValue = 0
        prefs.skill.minimumSkillOne = 1

        prefs.skill.shouldUpgradeSkillTwo = false
        prefs.skill.skillTwoUpgradeValue = 0
        prefs.skill.minimumSkillTwo = 1

        prefs.skill.shouldUpgradeSkillThree = false
        prefs.skill.skillThreeUpgradeValue = 0
        prefs.skill.minimumSkillThree = 1
    })
    FgaScreen {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                SkillUpgradeExitContent(
                    reason = exception.reason,
                    state = exception.state
                )
            }

            Divider()


            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    val allowCopy = exception.reason.let { reason ->
                        reason is AutoSkill.ExitReason.Unexpected && reason.e !is KnownException
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
private fun SkillUpgradeExitContent(
    reason: AutoSkill.ExitReason,
    state: AutoSkill.ExitState
) {
    Text(
        text = reason.text().uppercase(),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 5.dp)
    )

    val items = state.skillSummaryList

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
                    text = stringResource(id = R.string.skill_number, index + 1).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Divider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )
                SkillUpgradeSummary(
                    reason = reason,
                    summary = summary,
                )
            }

        }
    }

}

@Composable
private fun SkillUpgradeSummary(
    reason: AutoSkill.ExitReason,
    summary: AutoSkill.Summary,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            summary.startingLevel == 10 -> {
                item {
                    Text(
                        text = stringResource(id = R.string.skill_max_level).uppercase(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            summary.isAvailable && summary.isCheckToUpgrade -> {
                summaryLevelUp(reason = reason, summary = summary)
            }

            summary.isAvailable && !summary.isCheckToUpgrade -> {
                item {
                    Text(
                        text = stringResource(id = R.string.skill_not_selected_for_enhancement).uppercase(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            else -> {
                item {
                    Text(
                        text = stringResource(id = R.string.skill_not_available).uppercase(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }


    }
}

private fun LazyListScope.summaryLevelUp(
    reason: AutoSkill.ExitReason,
    summary: AutoSkill.Summary,
) {
    if (summary.startingLevel != null && summary.endLevel != null) {
        if (summary.startingLevel != summary.endLevel) {
            item {
                Text(
                    text = stringResource(
                        id = R.string.skill_enhancement_went_from_and_to,
                        summary.startingLevel!!,
                        summary.endLevel!!
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                val difference = summary.endLevel!! - summary.startingLevel!!
                Text(
                    text = stringResource(id = R.string.skill_enhancement_level_up_by, difference).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
    if (reason == AutoSkill.ExitReason.Abort) {
        if ((summary.startingLevel == summary.endLevel) ||
            summary.endLevel == null ||
            (summary.targetLevel != null && summary.endLevel != summary.targetLevel)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.enhancement_halt_aborted).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }


    summary.enhancementExitReason?.let {
        item {
            Text(
                text = it.reason.text().uppercase(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (summary.endLevel != summary.targetLevel && summary.targetLevel != null){
            item{
                Text(
                    text = stringResource(id = R.string.skill_enhancement_error_target_not_met,
                        summary.targetLevel!!
                    ).uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun AutoSkill.ExitReason.text(): String = when (this) {
    AutoSkill.ExitReason.RanOutOfQP -> stringResource(id = R.string.ran_out_of_qp)
    AutoSkill.ExitReason.Done -> stringResource(id = R.string.done)
    AutoSkill.ExitReason.NoServantSelected -> stringResource(id = R.string.enhancement_missing_servant)
    AutoSkill.ExitReason.Abort -> stringResource(R.string.stopped_by_user)
    is AutoSkill.ExitReason.Unexpected -> {
        e.let {
            if (it is KnownException) it.reason.msg
            else "${stringResource(R.string.unexpected_error)}: ${e.message}"
        }
    }
}

@Composable
private fun AutoSkill.EnhancementExitReason.text(): String = when (this) {
    AutoSkill.EnhancementExitReason.OutOfMatsException ->
        stringResource(id = R.string.enhancement_error_out_of_mats)

    AutoSkill.EnhancementExitReason.OutOfQPException ->
        stringResource(id = R.string.enhancement_error_out_of_qp)

    AutoSkill.EnhancementExitReason.ExitEarlyOutOfQPException ->
        stringResource(id = R.string.enhancement_error_exit_early_out_of_qp)

    AutoSkill.EnhancementExitReason.NoSkillUpgradeError ->
        stringResource(id = R.string.enhancement_error_no_enhancement)

    AutoSkill.EnhancementExitReason.TargetLevelMet -> stringResource(id = R.string.success)
}