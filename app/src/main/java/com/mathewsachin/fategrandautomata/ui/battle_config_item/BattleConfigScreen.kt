package com.mathewsachin.fategrandautomata.ui.battle_config_item

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.HeadingButton
import com.mathewsachin.fategrandautomata.ui.OnResume
import com.mathewsachin.fategrandautomata.ui.card_priority.getColorRes
import com.mathewsachin.fategrandautomata.ui.icon
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

    val battleConfigExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
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
    object SkillMaker: BattleConfigDestination()
    object CardPriority: BattleConfigDestination()
    object Spam: BattleConfigDestination()
    object PreferredSupport: BattleConfigDestination()
    object Back: BattleConfigDestination()
    class Other(val id: String): BattleConfigDestination()
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
                Heading(
                    stringResource(R.string.p_nav_battle_config_edit)
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
                        val context = LocalContext.current

                        HeadingButton(
                            text = stringResource(R.string.battle_config_item_delete),
                            isDanger = true,
                            icon = icon(Icons.Default.Delete),
                            onClick = {
                                AlertDialog.Builder(context)
                                    .setMessage(R.string.battle_config_item_delete_confirm_message)
                                    .setTitle(R.string.battle_config_item_delete_confirm_title)
                                    .setPositiveButton(R.string.battle_config_item_delete_confirm_ok) { _, _ -> onDelete() }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show()
                            }
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
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        config.materials.Materials()
                                    }

                                    Card(
                                        elevation = 3.dp,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.p_spam_spam),
                                            modifier = Modifier
                                                .clickable(onClick = { navigate(BattleConfigDestination.Spam) })
                                                .padding(16.dp, 5.dp)
                                        )
                                    }

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
        // Dark colors won't be visible in dark theme
        val score = if (MaterialTheme.colors.isLight)
            this
        else CardScore(CardType, CardAffinityEnum.Resist)

        return colorResource(score.getColorRes())
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
                        .padding(end = 16.dp)
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
                                        .padding(end = 4.dp)
                                )
                            }

                            Text(
                                it.toString(),
                                color = it.color
                            )
                        }
                    }
                }
            }
        }
    }
}