package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.ui.*
import com.mathewsachin.fategrandautomata.ui.card_priority.getColorRes
import com.mathewsachin.fategrandautomata.ui.pref_support.SupportViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.EditTextPreference
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import kotlinx.coroutines.launch

@Composable
fun BattleConfigScreen(
    vm: BattleConfigScreenViewModel = viewModel(),
    supportVm: SupportViewModel = viewModel(),
    navigate: (BattleConfigDestination) -> Unit
) {
    val context = LocalContext.current

    val battleConfigExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        vm.export(context, uri)
    }

    BattleConfigContent(
        config = vm.battleConfigCore,
        friendEntries = supportVm.friends,
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

    val scope = rememberCoroutineScope()

    OnResume {
        scope.launch {
            if (supportVm.shouldExtractSupportImages) {
                supportVm.performSupportImageExtraction(context)
            } else supportVm.refresh(context)
        }
    }
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
    friendEntries: Map<String, String>,
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
                item {
                    HeadingButton(
                        text = stringResource(R.string.battle_config_item_export),
                        onClick = onExport
                    )
                }

                item {
                    HeadingButton(
                        text = stringResource(R.string.battle_config_item_copy),
                        icon = icon(Icons.Default.ContentCopy),
                        onClick = onCopy
                    )
                }

                item {
                    HeadingButton(
                        text = stringResource(R.string.battle_config_item_delete),
                        isDanger = true,
                        icon = icon(Icons.Default.Delete),
                        onClick = { deleteConfirmDialog.show() }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Column {
                            config.name.EditTextPreference(
                                title = stringResource(R.string.p_battle_config_name),
                                validate = { it.isNotBlank() },
                                singleLine = true
                            )

                            Divider()

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
                            .padding(bottom = 16.dp)
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
                            .padding(bottom = 16.dp)
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

                                ServerSelection(config)

                                VerticalDivider()

                                PartySelection(config)
                            }

                            Divider()

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
                        config = config,
                        goToPreferred = { navigate(BattleConfigDestination.PreferredSupport) },
                        maxSkillText = maxSkillText,
                        friendEntries = friendEntries
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

                Card {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                    ) {
                        priorities.forEachIndexed { index, it ->
                            if (index != 0) {
                                Text(
                                    ",",
                                    modifier = Modifier
                                        .padding(end = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                it.toString(),
                                color = it.color,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    shadow = Shadow(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        offset = Offset(1f, 1f),
                                        blurRadius = 0f
                                    )
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}