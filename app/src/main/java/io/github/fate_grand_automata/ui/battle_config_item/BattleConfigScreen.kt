package io.github.fate_grand_automata.ui.battle_config_item

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.models.CardPriorityPerWave
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.HeadingButton
import io.github.fate_grand_automata.ui.VerticalDivider
import io.github.fate_grand_automata.ui.card_priority.getColorRes
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.prefs.EditTextPreference
import io.github.fate_grand_automata.ui.prefs.Preference
import io.github.fate_grand_automata.util.toSp

@Composable
fun BattleConfigScreen(
    vm: BattleConfigScreenViewModel = viewModel(),
    navigate: (BattleConfigDestination) -> Unit
) {
    val context = LocalContext.current

    val battleConfigExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        vm.export(context, uri)
    }

    BattleConfigContent(
        config = vm.battleConfigCore,
        onExport = { battleConfigExport.launch("${vm.battleConfig.name}.fga") },
        onCopy = {
            val id = vm.createCopyAndReturnId(context)
            navigate(BattleConfigDestination.Other(id))
        },
        onDelete = {
            vm.delete()
            navigate(BattleConfigDestination.Back)
        },
        navigate = navigate
    )
}

sealed class BattleConfigDestination {
    object SkillMaker : BattleConfigDestination()
    object CardPriority : BattleConfigDestination()
    object Spam : BattleConfigDestination()
    object PreferredSupport : BattleConfigDestination()
    object Back : BattleConfigDestination()
    class Other(val id: String) : BattleConfigDestination()
}

@Composable
private fun BattleConfigContent(
    config: BattleConfigCore,
    onExport: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    navigate: (BattleConfigDestination) -> Unit,
    vm: BattleConfigScreenViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val deleteConfirmDialog = FgaDialog()
            deleteConfirmDialog.build {
                title(stringResource(R.string.battle_config_item_delete_confirm_title))
                message(stringResource(R.string.battle_config_item_delete_confirm_message))

                buttons(
                    onSubmit = onDelete,
                    okLabel = stringResource(R.string.battle_config_item_delete_confirm_ok)
                )
            }

            Heading(
                stringResource(R.string.battle_config_edit)
            ) {
                HeadingButton(
                    text = stringResource(R.string.battle_config_item_export),
                    onClick = onExport
                )

                HeadingButton(
                    text = stringResource(R.string.battle_config_item_copy),
                    icon = icon(Icons.Default.ContentCopy),
                    onClick = onCopy
                )

                HeadingButton(
                    text = stringResource(R.string.battle_config_item_delete),
                    isDanger = true,
                    icon = icon(Icons.Default.Delete),
                    onClick = { deleteConfirmDialog.show() }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column {
                            config.name.EditTextPreference(
                                title = stringResource(R.string.p_battle_config_name),
                                validate = { it.isNotBlank() },
                                singleLine = true
                            )

                            HorizontalDivider()

                            config.notes.EditTextPreference(
                                title = stringResource(R.string.p_battle_config_notes)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        SkillCommandGroup(
                            config = config,
                            vm = vm,
                            openSkillMaker = { navigate(BattleConfigDestination.SkillMaker) }
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    config.materials.Materials()
                                }

                                VerticalDivider()

                                ServerSelection(config)

                                VerticalDivider()

                                PartySelection(config)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clickable(onClick = { navigate(BattleConfigDestination.Spam) }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        stringResource(R.string.p_spam_spam).uppercase(),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .padding(16.dp, 5.dp)
                                    )
                                }

                                VerticalDivider()
                                
                                StoryIntro(config = config)
                            }

                            HorizontalDivider()

                            val cardPriority by vm.cardPriority.collectAsState(null)

                            cardPriority?.let {
                                Preference(
                                    title = { Text(stringResource(R.string.p_battle_config_card_priority)) },
                                    summary = { CardPrioritySummary(it) },
                                    onClick = { navigate(BattleConfigDestination.CardPriority) }
                                )
                            }
                        }
                    }
                }

                item {
                    val maxSkillText by vm.maxSkillText.collectAsState("")

                    SupportGroup(
                        config = config.support,
                        goToPreferred = { navigate(BattleConfigDestination.PreferredSupport) },
                        maxSkillText = maxSkillText
                    )
                }

                item {
                    ShuffleCardsGroup(config)
                }
            }
        }
    }
}

private val CardScore.color: Color
    @Composable get() {
        return colorResource(getColorRes())
    }

@Composable
private fun CardPrioritySummary(cardPriority: CardPriorityPerWave) {
    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
    ) {
        cardPriority.forEachIndexed { wave, priorities ->
            Row(
                modifier = Modifier
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "W${wave + 1}: ",
                    modifier = Modifier
                        .padding(end = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    val priorityString = buildAnnotatedString {
                        priorities.forEachIndexed { index, it ->
                            if (index != 0) {
                                withStyle(
                                    style = SpanStyle(
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        letterSpacing = MaterialTheme.typography.bodyMedium.letterSpacing,
                                    )
                                ) {
                                    append(",")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        letterSpacing = 4.dp.toSp()
                                    )
                                ) {
                                    append(" ")
                                }
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                    fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    letterSpacing = MaterialTheme.typography.bodyMedium.letterSpacing,
                                    color = it.color,
                                    shadow = Shadow(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        offset = Offset(1f, 1f),
                                        blurRadius = 0f
                                    )
                                )
                            ) {
                                append(it.toString())
                            }
                        }
                    }
                    Text(
                        text = priorityString,
                        modifier = Modifier.padding(horizontal = 5.dp),
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }
}