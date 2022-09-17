package com.mathewsachin.fategrandautomata.ui.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences

@Composable
fun ceBombLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier
): ScriptLauncherResponseBuilder {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp)
    ) {
        Text(
            "Start the CE Bomb script",
            style = MaterialTheme.typography.titleLarge
        )

        Divider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            "This script will consume any CE it can indiscriminately.\n" +
                    "Please ensure you've locked every CE you don't want to lose, " +
                    "or moved them to the Second Archive.",
            style = MaterialTheme.typography.bodyLarge
        )
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            // TODO : since the targetRarity doesn't matter anymore
            // I'm just passing an arbitrary value as i have no idea how it works behind that
            ScriptLauncherResponse.CEBomb(1)
        }
    )
}