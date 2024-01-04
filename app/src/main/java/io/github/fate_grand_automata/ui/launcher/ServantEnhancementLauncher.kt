package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.VerticalDivider
import io.github.fate_grand_automata.ui.prefs.remember


@Composable
fun servantEnhancementLauncher(
    prefsCore: PrefsCore,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    var shouldLimit by prefsCore.servantEnhancement.shouldLimit.remember()

    var limitCount by prefsCore.servantEnhancement.limitCount.remember()

    var shouldRedirectAscension by prefsCore.servantEnhancement.shouldRedirectAscension.remember()

    var shouldPerformAscension by prefsCore.servantEnhancement.shouldPerformAscension.remember()

    var shouldRedirectGrail by prefsCore.servantEnhancement.shouldRedirectGrail.remember()

    var muteNotifications by prefsCore.servantEnhancement.muteNotifications.remember()

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.servant_enhancement),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()
            }
        }
        item {
            Text(
                text = stringResource(R.string.note),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = stringResource(id = R.string.servert_enhancement_warning_notice),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
        }
        item {
            Divider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shouldLimit = !shouldLimit }
            ) {
                Text(
                    stringResource(R.string.servant_enhancement_limit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Switch(
                        checked = shouldLimit,
                        onCheckedChange = { shouldLimit = it }
                    )
                    Stepper(
                        value = limitCount,
                        onValueChange = { limitCount = it },
                        valueRange = 1..999,
                        enabled = shouldLimit
                    )
                }
            }
        }

        item {
            Divider(
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                RowTextCheckBox(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.servant_redirect_ascension_question),
                    status = shouldRedirectAscension,
                    onStatusChange = {
                        shouldRedirectAscension = it
                    }
                )
                VerticalDivider()

                RowTextCheckBox(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.servant_redirect_grail),
                    status = shouldRedirectGrail,
                    onStatusChange = {
                        shouldRedirectGrail = it
                    }
                )

            }
        }
        item {
            Divider(
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                RowTextCheckBox(
                    modifier = Modifier
                        .weight(1f),
                    text = stringResource(R.string.servant_perform_ascension_question),
                    status = shouldPerformAscension,
                    onStatusChange = {
                        shouldPerformAscension = it
                    }
                )
                VerticalDivider()
                Spacer(modifier = Modifier.weight(1f))

            }
        }
        item {
            Divider(
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }
        item {
            RowTextCheckBox(
                modifier = Modifier
                    .fillMaxWidth(),
                text = stringResource(R.string.mute_notifications),
                status = muteNotifications,
                onStatusChange = {
                    muteNotifications = it
                }
            )
        }
    }


    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.ServantEnhancement
        }
    )
}

@Composable
private fun RowTextCheckBox(
    modifier: Modifier = Modifier,
    status: Boolean,
    text: String,
    onStatusChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable { onStatusChange(!status) }
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Justify,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = status,
            onCheckedChange = { onStatusChange(it) }
        )
    }
}