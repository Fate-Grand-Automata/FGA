package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.Stepper

@Composable
fun fpLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    var shouldLimit by remember { mutableStateOf(prefs.shouldLimitFP) }
    var rollLimit by remember { mutableStateOf(prefs.limitFP) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            stringResource(R.string.p_script_mode_fp),
            style = MaterialTheme.typography.titleLarge
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shouldLimit = !shouldLimit }
        ) {
            Text(
                stringResource(R.string.p_roll_limit),
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
                value = rollLimit,
                onValueChange = { rollLimit = it },
                valueRange = 1..999,
                enabled = shouldLimit
            )
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.FP(
                if (shouldLimit) rollLimit else null
            )
        }
    )
}