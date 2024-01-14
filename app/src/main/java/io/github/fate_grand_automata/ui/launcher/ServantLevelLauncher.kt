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
import io.github.fate_grand_automata.prefs.core.ServantEnhancementPrefsCore
import io.github.fate_grand_automata.ui.VerticalDivider
import io.github.fate_grand_automata.ui.prefs.remember


@Composable
fun servantLevelLauncher(
    servantEnhancementPrefsCore: ServantEnhancementPrefsCore,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var shouldRedirectAscension by servantEnhancementPrefsCore.shouldRedirectAscension.remember()

    var shouldPerformAscension by servantEnhancementPrefsCore.shouldPerformAscension.remember()

    var shouldRedirectGrail by servantEnhancementPrefsCore.shouldRedirectGrail.remember()

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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                RowTextCheckBox(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.servant_enhancement_redirect_ascension_question),
                    status = shouldRedirectAscension,
                    onStatusChange = {
                        shouldRedirectAscension = it
                    }
                )
                VerticalDivider()

                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,

                    ) {
                    RowTextCheckBox(
                        text = stringResource(R.string.servant_enhancement_perform_ascension_question),
                        status = shouldPerformAscension,
                        onStatusChange = {
                            shouldPerformAscension = it
                        }
                    )
                    Text(
                        stringResource(R.string.servant_enhancement_perform_ascension_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
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
                        text = stringResource(R.string.servant_enhancement_redirect_grail),
                        status = shouldRedirectGrail,
                        onStatusChange = {
                            shouldRedirectGrail = it
                        }
                    )
                VerticalDivider()
                Spacer(modifier = Modifier.weight(1f))
            }
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