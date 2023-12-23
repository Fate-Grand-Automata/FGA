package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.ui.Stepper
import io.github.fate_grand_automata.ui.prefs.remember


@Composable
fun servantEnhancementLauncher(
    prefsCore: PrefsCore,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {

    var shouldLimit by remember {
        mutableStateOf(false)
    }

    var limitCount by remember {
        mutableStateOf(1)
    }

    var shouldRedirectAscension by prefsCore.servantEnhancement.shouldRedirectAscension.remember()

    var shouldRedirectGrail by prefsCore.servantEnhancement.shouldRedirectGrail.remember()

    DisposableEffect(Unit){
        onDispose {
            prefsCore.servantEnhancement.limitCount.set(limitCount)
            prefsCore.servantEnhancement.shouldLimit.set(shouldLimit)
        }
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            text = stringResource(id = R.string.servant_enhancement),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = stringResource(id = R.string.servert_enhancement_warning_notice),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify
        )

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

            Switch(
                checked = shouldLimit,
                onCheckedChange = { shouldLimit = it }
            )
        }

        Box(
            modifier = Modifier.align(Alignment.End)
        ) {
            Stepper(
                value = limitCount,
                onValueChange = { limitCount = it },
                valueRange = 1..999,
                enabled = shouldLimit
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shouldRedirectAscension = !shouldRedirectAscension }
        ) {
            Text(
                stringResource(R.string.servant_enhancement_redirect_ascension),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Switch(
                checked = shouldRedirectAscension,
                onCheckedChange = { shouldRedirectAscension = it }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shouldRedirectGrail = !shouldRedirectGrail }
        ) {
            Text(
                stringResource(R.string.servant_enhancement_redirect_grail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Switch(
                checked = shouldRedirectGrail,
                onCheckedChange = { shouldRedirectGrail = it }
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