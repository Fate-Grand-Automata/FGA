package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand
import com.mathewsachin.fategrandautomata.ui.DimmedIcon
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceTextEditor
import com.mathewsachin.fategrandautomata.ui.prefs.remember
import com.mathewsachin.fategrandautomata.ui.skill_maker.SkillMakerEntry
import com.mathewsachin.fategrandautomata.ui.skill_maker.colorRes

@Composable
fun SkillCommandGroup(
    config: BattleConfigCore,
    vm: BattleConfigScreenViewModel,
    openSkillMaker: () -> Unit
) {
    var cmd by config.skillCommand.remember()
    val parsedCommand by vm.skillCommand.collectAsState(listOf())
    var editing by remember { mutableStateOf(false) }

    if (editing) {
        var errorMessage by remember { mutableStateOf("") }

        Column {
            PreferenceTextEditor(
                label = stringResource(R.string.p_battle_config_cmd),
                prefill = cmd,
                onSubmit = {
                    try {
                        // Check if parses correctly
                        AutoSkillCommand.parse(it)

                        cmd = it
                        editing = false
                        errorMessage = ""
                    } catch (e: Exception) {
                        // TODO: Localize
                        errorMessage = "Invalid skill command"
                    }
                },
                onCancel = { editing = false },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )

            if (errorMessage.isNotBlank()) {
                PreferenceError(errorMessage)
            }
        }
    } else {
        Preference(
            title = { Text(stringResource(R.string.p_battle_config_cmd)) },
            summary = if (parsedCommand.isNotEmpty()) {
                { SkillCommandSummary(parsedCommand) }
            } else null,
            onClick = openSkillMaker
        ) {
            IconButton(
                onClick = { editing = true }
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_terminal),
                    contentDescription = "Show Textbox for editing Skill command"
                )
            }
        }
    }
}

// FIXME: Scrolling commands crashes the app (as of compose-beta09) -> https://issuetracker.google.com/issues/189965769 looks related
@Composable
fun SkillCommandSummary(skillCommand: List<SkillMakerEntry>) {
    LazyRow(
        modifier = Modifier
            .padding(vertical = 2.dp)
    ) {
        items(skillCommand) {
            Card(
                colors = cardColors(
                    containerColor = colorResource(it.colorRes)
                ),
                modifier = Modifier
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    it.toString(),
                    color = Color.White,
                    modifier = Modifier
                        .padding(2.dp, 1.dp)
                )
            }
        }
    }
}

@Composable
fun PreferenceError(error: String) {
    Text(
        error,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .padding(16.dp, 2.dp)
            .padding(bottom = 5.dp)
    )
}