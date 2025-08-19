package io.github.fate_grand_automata.ui.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.dialog.ChoiceListItem

@Composable
fun ceBombLauncher(
    prefs: IPreferences,
    modifier: Modifier = Modifier,
): ScriptLauncherResponseBuilder {
    var target by remember { mutableStateOf(prefs.ceBombTargetRarity) }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 5.dp),
    ) {
        Text(
            "Choose CE Bomb target",
            style = MaterialTheme.typography.titleLarge,
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(5.dp)
                .padding(bottom = 16.dp),
        )

        LazyColumn {
            items((1..2).toList()) {
                ChoiceListItem(
                    isSelected = target == it,
                    onClick = { target = it },
                ) {
                    Text("$it\u2605 CEs")
                }
            }
        }
    }

    return ScriptLauncherResponseBuilder(
        canBuild = { true },
        build = {
            ScriptLauncherResponse.CEBomb(
                targetRarity = target,
            )
        },
    )
}
