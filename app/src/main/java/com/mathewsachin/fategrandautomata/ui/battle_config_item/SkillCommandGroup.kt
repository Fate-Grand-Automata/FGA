package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.ui.prefs.PreferenceTextEditor
import com.mathewsachin.fategrandautomata.ui.prefs.collect
import com.mathewsachin.fategrandautomata.ui.skill_maker.SkillMakerEntry
import com.mathewsachin.fategrandautomata.ui.skill_maker.colorRes

@Composable
fun SkillCommandGroup(
    config: BattleConfigCore,
    vm: BattleConfigItemViewModel,
    openSkillMaker: () -> Unit
) {
    val cmd by config.skillCommand.collect()
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

                        config.skillCommand.set(it)
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
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier
                        .padding(16.dp, 2.dp)
                )
            }
        }
    }
    else {
        Preference(
            title = { Text(stringResource(R.string.p_battle_config_cmd)) },
            summary = if (parsedCommand.isNotEmpty()) {
                { SkillCommandSummary(parsedCommand) }
            } else null,
            onClick = openSkillMaker
        ) {
            Icon(
                painterResource(R.drawable.ic_terminal),
                contentDescription = "Show Textbox for editing Skill command",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { editing = true }
                    .padding(7.dp)
            )
        }
    }
}

@Composable
fun SkillCommandSummary(skillCommand: List<SkillMakerEntry>) {
    LazyRow(
        modifier = Modifier
            .padding(vertical = 2.dp)
    ) {
        items(skillCommand) {
            Surface(
                color = colorResource(it.colorRes),
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