package io.github.fate_grand_automata.ui.exit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.entrypoints.AutoSkillUpgrade
import io.github.fate_grand_automata.scripts.entrypoints.AutoSkillUpgrade.SkillResult
import io.github.fate_grand_automata.ui.FGATheme
import io.github.fate_grand_automata.util.KnownException
import io.github.fate_grand_automata.util.displayMessage

@Composable
fun SkillExit(
    exception: AutoSkillUpgrade.ExitException,
    onClose: () -> Unit,
    onCopy: () -> Unit
) {
    FGATheme {
        Column {
            Text(
                exception.reason.text(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                exception.summaries.forEachIndexed { index, summary ->
                    SkillSummary(
                        skillNumber = index + 1,
                        summary = summary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth()) {
                val reason = exception.reason
                if (reason is AutoSkillUpgrade.ExitReason.Unexpected && reason.cause !is KnownException) {
                    TextButton(onClick = onCopy) {
                        Text(stringResource(R.string.unexpected_error_copy))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onClose) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}

@Composable
private fun SkillSummary(
    skillNumber: Int,
    summary: AutoSkillUpgrade.Summary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            stringResource(R.string.skill_upgrade_skill_number, skillNumber),
            style = MaterialTheme.typography.bodyLarge
        )

        val startLevel = summary.startLevel
        val endLevel = summary.endLevel
        if (startLevel != null && endLevel != null && endLevel > startLevel) {
            Text(
                stringResource(R.string.skill_upgrade_level_change, startLevel, endLevel),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        summary.resultText()?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Null when the level change alone says everything.
 */
@Composable
private fun AutoSkillUpgrade.Summary.resultText(): String? = when (result) {
    SkillResult.Locked -> stringResource(R.string.skill_upgrade_locked)
    SkillResult.Unreadable -> stringResource(R.string.skill_upgrade_unreadable)
    SkillResult.AlreadyMaxed -> stringResource(R.string.skill_upgrade_max_level)
    SkillResult.NotSelected -> stringResource(R.string.skill_upgrade_not_selected)
    SkillResult.TargetReached -> null
    SkillResult.OutOfMaterials -> stringResource(R.string.skill_upgrade_out_of_materials)
    SkillResult.OutOfQP -> stringResource(R.string.skill_upgrade_out_of_qp)
    SkillResult.NotFinished -> stringResource(R.string.skill_upgrade_not_finished, targetLevel)
}

@Composable
private fun AutoSkillUpgrade.ExitReason.text(): String = when (this) {
    AutoSkillUpgrade.ExitReason.Done -> stringResource(R.string.skill_upgrade_done)
    AutoSkillUpgrade.ExitReason.RanOutOfQP -> stringResource(R.string.skill_upgrade_ran_out_of_qp)
    AutoSkillUpgrade.ExitReason.NoServantSelected -> stringResource(R.string.servant_enhancement_none_selected)
    AutoSkillUpgrade.ExitReason.ServantDeselected -> stringResource(R.string.skill_upgrade_servant_deselected)
    AutoSkillUpgrade.ExitReason.NoProgress -> stringResource(R.string.skill_upgrade_no_progress)
    AutoSkillUpgrade.ExitReason.Abort -> stringResource(R.string.stopped_by_user)
    is AutoSkillUpgrade.ExitReason.Unexpected -> cause.displayMessage(LocalContext.current)
}

@Preview(widthDp = 600, heightDp = 300)
@Composable
private fun PreviewSkillExit() {
    SkillExit(
        exception = AutoSkillUpgrade.ExitException(
            reason = AutoSkillUpgrade.ExitReason.RanOutOfQP,
            summaries = listOf(
                AutoSkillUpgrade.Summary(4, 10, 10, SkillResult.TargetReached),
                AutoSkillUpgrade.Summary(6, 8, 10, SkillResult.OutOfQP),
                AutoSkillUpgrade.Summary(null, null, 0, SkillResult.Locked)
            )
        ),
        onClose = {},
        onCopy = {}
    )
}
