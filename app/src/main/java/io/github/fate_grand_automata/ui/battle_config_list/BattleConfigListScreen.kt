package io.github.fate_grand_automata.ui.battle_config_list

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.scripts.enums.BattleConfigListSortEnum
import io.github.fate_grand_automata.ui.*
import io.github.fate_grand_automata.ui.battle_config_item.Material
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.dialog.singleChoiceList
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.simpleStringRes
import io.github.fate_grand_automata.util.stringRes
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
fun BattleConfigListScreen(
    vm: BattleConfigListViewModel = viewModel(),
    navigate: (String) -> Unit
) {
    val selectionMode by vm.selectionMode.collectAsState()
    val selectedConfigs by vm.selectedConfigs.collectAsState()

    val configListSort by vm.configListSort.collectAsState(BattleConfigListSortEnum.DEFAULT_SORT)

    BackHandler(
        enabled = selectionMode,
        onBack = { vm.endSelection() }
    )

    val context = LocalContext.current

    val battleConfigsExport = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { dirUri ->
        vm.exportBattleConfigs(context, dirUri)
    }

    val battleConfigImport = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        vm.importBattleConfigs(context, uris)
    }

    val deleteConfirmDialog = FgaDialog()
    deleteConfirmDialog.build {
        title(stringResource(R.string.battle_config_list_delete_confirm_title))
        message(stringResource(R.string.battle_config_list_delete_confirm_message, selectedConfigs.size))

        buttons(
            onSubmit = { vm.deleteSelected() }
        )
    }

    val configs by vm.battleConfigItems.collectAsState(emptyList())

    BattleConfigListContent(
        configs = configs,
        configListSort = configListSort,
        onConfigListSortChange = {
            vm.setConfigListSort(it)
        },
        selectionMode = selectionMode,
        selectedConfigs = selectedConfigs,
        action = {
            when (it) {
                BattleConfigListAction.AddNew -> {
                    navigate(vm.newConfig().id)
                }

                BattleConfigListAction.Delete -> deleteConfirmDialog.show()
                is BattleConfigListAction.Edit -> {
                    navigate(it.id)
                }

                BattleConfigListAction.Export -> battleConfigsExport.launch(Uri.EMPTY)
                BattleConfigListAction.Import -> battleConfigImport.launch(
                    //octet-stream as backup in case Android doesn't detect json
                    arrayOf("application/json", "application/octet-stream")
                )

                is BattleConfigListAction.ToggleSelected -> vm.toggleSelected(it.id)
                is BattleConfigListAction.StartSelection -> vm.startSelection(it.id)
            }
        }
    )
}

private sealed class BattleConfigListAction {
    object Export : BattleConfigListAction()
    object Import : BattleConfigListAction()
    object Delete : BattleConfigListAction()
    object AddNew : BattleConfigListAction()
    class ToggleSelected(val id: String) : BattleConfigListAction()
    class StartSelection(val id: String) : BattleConfigListAction()
    class Edit(val id: String) : BattleConfigListAction()
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun BattleConfigListContent(
    configs: List<BattleConfigCore>,
    configListSort: BattleConfigListSortEnum,
    onConfigListSortChange: (BattleConfigListSortEnum) -> Unit,
    selectionMode: Boolean,
    selectedConfigs: Set<String>,
    action: (BattleConfigListAction) -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val sortListDialog = FgaDialog()

    sortListDialog.build {
        title(
            text = stringResource(id = R.string.p_battle_config_sort_title)
        )

        singleChoiceList(
            items = BattleConfigListSortEnum.entries,
            selected = configListSort,
            onSelectedChange = { sort ->
                onConfigListSortChange(sort)
                sortListDialog.hide()
            },
            template = {
                Text(
                    text = stringResource(id = it.stringRes)
                )
            }
        )
    }

    Box {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Heading(
                    stringResource(R.string.p_battle_config)
                ) {
                    HeadingButton(
                        text = stringResource(
                            if (selectionMode)
                                R.string.battle_config_item_export
                            else R.string.battle_config_list_export_all
                        ),
                        onClick = { action(BattleConfigListAction.Export) }
                    )

                    Crossfade(
                        selectionMode,
                        label = "Selection mode"
                    ) {
                        if (it) {
                            HeadingButton(
                                text = stringResource(R.string.battle_config_list_delete),
                                onClick = { action(BattleConfigListAction.Delete) },
                                isDanger = true,
                                icon = icon(Icons.Default.Delete)
                            )
                        } else {
                            HeadingButton(
                                text = stringResource(R.string.battle_config_list_import),
                                onClick = { action(BattleConfigListAction.Import) }
                            )
                        }
                    }

                    HeadingButton(
                        text = stringResource(id = R.string.p_battle_config_sort),
                        onClick = {
                            sortListDialog.show()
                        },
                        icon = icon(Icons.Default.Sort)
                    )
                }

                val servers by derivedStateOf {
                    configs
                        .mapNotNull { it.server.get().asGameServer() }
                        .distinct()
                }

                if (servers.isEmpty()) {
                    ConfigList(
                        configs = configs,
                        selectionMode = selectionMode,
                        action = action,
                        selectedConfigs = selectedConfigs,
                        modifier = Modifier
                            .weight(1f)
                    )
                } else {
                    Tabbed(
                        items = listOf<BattleConfigCore.Server>(BattleConfigCore.Server.NotSet) + servers.map { BattleConfigCore.Server.Set(it) },
                        heading = {
                            Text(
                                when (it) {
                                    BattleConfigCore.Server.NotSet -> "ALL"
                                    is BattleConfigCore.Server.Set -> stringResource(it.server.stringRes)
                                }
                            )
                        },
                        content = { current ->
                            val filteredConfigs by derivedStateOf {
                                configs
                                    .filter {
                                        val server = it.server.get().asGameServer()

                                        current is BattleConfigCore.Server.NotSet
                                                || server == current.asGameServer()
                                    }
                            }

                            ConfigList(
                                configs = filteredConfigs,
                                selectionMode = selectionMode,
                                action = action,
                                selectedConfigs = selectedConfigs
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(if (isLandscape) Alignment.TopEnd else Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            val enterAnimation = if (isLandscape)
                slideInHorizontally(initialOffsetX = { it / 2 })
            else slideInVertically(initialOffsetY = { it / 2 })

            val exitAnimation = if (isLandscape)
                slideOutHorizontally(targetOffsetX = { it * 2 })
            else slideOutVertically(targetOffsetY = { it * 2 })

            AnimatedVisibility(
                !selectionMode,
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                FloatingActionButton(
                    onClick = { action(BattleConfigListAction.AddNew) },
                    modifier = Modifier
                        .scale(if (isLandscape) 0.7f else 1f),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create new config",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigList(
    configs: List<BattleConfigCore>,
    selectionMode: Boolean,
    action: (BattleConfigListAction) -> Unit,
    selectedConfigs: Set<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        if (configs.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.battle_config_list_no_items),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val configsByServer = configs.groupBy { it.server.get().asGameServer() }
            configsByServer.forEach { entry ->
                if (configsByServer.size > 1) {
                    stickyHeader {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondary)
                                .padding(10.dp, 5.dp),
                        ) {
                            Text(
                                text = stringResource(entry.key?.simpleStringRes ?: R.string.p_not_set),
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Left,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }

                items(
                    entry.value,
                    key = { it.id }
                ) {
                    BattleConfigListItem(
                        it,
                        onClick = {
                            if (selectionMode) {
                                action(BattleConfigListAction.ToggleSelected(it.id))
                            } else {
                                action(BattleConfigListAction.Edit(it.id))
                            }
                        },
                        onLongClick = {
                            if (!selectionMode) {
                                action(BattleConfigListAction.StartSelection(it.id))
                            }
                        },
                        isSelectionMode = selectionMode,
                        isSelected = selectionMode && it.id in selectedConfigs
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }


        }
    }
}

@Composable
private fun BattleConfigItemSelected(
    isSelectionMode: Boolean,
    isSelected: Boolean
) {
    AnimatedVisibility(isSelectionMode) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(end = 16.dp)
                .border(
                    1.dp,
                    if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    CircleShape
                )
                .background(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent
                )
                .size(15.dp)
        ) {
            AnimatedVisibility(isSelected) {
                Icon(
                    rememberVectorPainter(Icons.Default.Check),
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun BattleConfigListItem(
    config: BattleConfigCore,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val name by config.name.remember()
    val materialsSet by config.materials.remember()
    val mats = materialsSet.take(3)

    // Without this, holding a list item would leave it highlighted because of recomposition happening before ripple ending
    val longClickState = rememberUpdatedState(onLongClick)

    val usageCount by config.usageCount.remember()
    val lastUsage by config.lastUsage.remember()

    val usageContent: @Composable (() -> Unit)? = if (usageCount > 0) {
        @Composable {
            val lastUsageSystemDateTime = lastUsage
                .toInstant(TimeZone.UTC)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    stringResource(
                        R.string.battle_config_list_usage_count,
                        usageCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(
                        R.string.battle_config_list_last_usage,
                        lastUsageSystemDateTime.date.toString(),
                        lastUsageSystemDateTime.time.hour,
                        lastUsageSystemDateTime.time.minute,
                        lastUsageSystemDateTime.time.second,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else null

    ListItem(
        leadingContent = {
            BattleConfigItemSelected(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )
        },
        headlineContent = {
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            mats.forEach {
                Material(it)
            }
        },
        modifier = Modifier
            .padding(5.dp)
            .clip(
                RoundedCornerShape(25)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    longClickState.value.invoke()
                }
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(25)
            ),
        supportingContent = usageContent,
        tonalElevation = if (isSelected) 5.dp else 1.dp,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,

            )
    )
}