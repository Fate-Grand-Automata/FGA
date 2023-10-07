package io.github.fate_grand_automata.ui.exit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.prefs.core.PrefsCore
import io.github.fate_grand_automata.scripts.entrypoints.AutoSkillUpgrade
import io.github.fate_grand_automata.scripts.prefs.IPreferences
import io.github.fate_grand_automata.ui.FgaScreen


@Composable
fun SkillUpgradeExit(
    exception: AutoSkillUpgrade.ExitException,
    prefs: IPreferences,
    prefsCore: PrefsCore,
    onClose: () -> Unit,
    onCopy: () -> Unit
) {
    LaunchedEffect(key1 = Unit, block = {
        prefs.skillUpgrade.shouldUpgradeSkill1 = false
        prefs.skillUpgrade.upgradeSkill1 = 0
        prefs.skillUpgrade.minSkill1 = 1

        prefs.skillUpgrade.shouldUpgradeSkill2 = false
        prefs.skillUpgrade.upgradeSkill2 = 0
        prefs.skillUpgrade.minSkill2 = 1

        prefs.skillUpgrade.shouldUpgradeSkill3 = false
        prefs.skillUpgrade.upgradeSkill3 = 0
        prefs.skillUpgrade.minSkill3 = 1
    })
    FgaScreen {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                skillUpgradeExitContent(
                    reason = exception.reason
                )
            }

            Divider()


            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {


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


private fun LazyListScope.skillUpgradeExitContent(
    reason: AutoSkillUpgrade.ExitReason
) {
    item {
        Text(
            text = reason.text(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp, top = 5.dp)
        )
    }
}

@Composable
private fun AutoSkillUpgrade.ExitReason.text(): String = when (this) {
    AutoSkillUpgrade.ExitReason.RanOutOfQP -> "Run out of QP"
    AutoSkillUpgrade.ExitReason.Success -> "Success"
}