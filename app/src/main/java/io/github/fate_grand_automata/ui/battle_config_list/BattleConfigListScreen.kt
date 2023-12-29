package io.github.fate_grand_automata.ui.battle_config_list

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.BattleConfigCore
import io.github.fate_grand_automata.ui.*
import io.github.fate_grand_automata.ui.battle_config_item.Material
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes

@Composable
fun BattleConfigListScreen(
    vm: BattleConfigListViewModel = viewModel(),
    windowSizeClass: WindowSizeClass,
    navigate: (String) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val selectionMode by vm.selectionMode.collectAsState()
    val selectedConfigs by vm.selectedConfigs.collectAsState()

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
        selectionMode = selectionMode,
        selectedConfigs = selectedConfigs,
        windowSizeClass = windowSizeClass,
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
                BattleConfigListAction.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    )
}

private sealed class BattleConfigListAction {

    data object NavigateBack : BattleConfigListAction()
    data object Export : BattleConfigListAction()
    data object Import : BattleConfigListAction()
    data object Delete : BattleConfigListAction()
    data object AddNew : BattleConfigListAction()
    class ToggleSelected(val id: String) : BattleConfigListAction()
    class StartSelection(val id: String) : BattleConfigListAction()
    class Edit(val id: String) : BattleConfigListAction()
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun BattleConfigListContent(
    windowSizeClass: WindowSizeClass,
    configs: List<BattleConfigCore>,
    selectionMode: Boolean,
    selectedConfigs: Set<String>,
    action: (BattleConfigListAction) -> Unit
) {
    val isLandscape = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    var fabHeight by remember { mutableIntStateOf(0) }

    val fabHeightInDp = with(LocalDensity.current) { fabHeight.toDp() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.p_battle_config).uppercase(),
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                actions ={
                    if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) {
                        CreateConfigFab(
                            modifier = Modifier,
                            isLandscape = isLandscape,
                            selectionMode = selectionMode,
                            action = action
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            action(BattleConfigListAction.NavigateBack)
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                CreateConfigFab(
                    modifier = Modifier
                        .onGloballyPositioned {
                            fabHeight = it.size.height
                        },
                    isLandscape = isLandscape,
                    selectionMode = selectionMode,
                    action = action
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
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
                    label = "selection Mode"
                ) { selMode ->
                    if (selMode) {
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
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier
                        .weight(1f)
                )
            } else {
                Tabbed(
                    items = listOf<BattleConfigCore.Server>(BattleConfigCore.Server.NotSet) +
                            servers.map { BattleConfigCore.Server.Set(it) },
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
                            selectedConfigs = selectedConfigs,
                            windowSizeClass = windowSizeClass,
                            fabHeightPadding = fabHeightInDp,
                        )
                    },
                    modifier = Modifier
                        .weight(1f),

                    )
            }
        }

    }
}

@Composable
private fun CreateConfigFab(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
    selectionMode: Boolean,
    action: (BattleConfigListAction) -> Unit
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
            modifier = modifier
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

@Composable
private fun ConfigList(
    modifier: Modifier = Modifier,
    configs: List<BattleConfigCore>,
    selectionMode: Boolean,
    action: (BattleConfigListAction) -> Unit,
    selectedConfigs: Set<String>,
    windowSizeClass: WindowSizeClass,
    fabHeightPadding: Dp = 0.dp
) {
    LazyVerticalGrid(
        columns = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Medium -> GridCells.Fixed(2)
            WindowWidthSizeClass.Expanded -> GridCells.Fixed(3)
            else -> GridCells.Fixed(1)
        },
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = fabHeightPadding +
                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) 16.dp else 0.dp
        ),
        modifier = modifier
    ) {
        if (configs.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.battle_config_list_no_items),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            configs.forEach { config ->
                item(
                    config.id,
                    span = {
                        GridItemSpan(1)
                    }
                ) {
                    BattleConfigListItem(
                        config,
                        onClick = {
                            if (selectionMode) {
                                action(BattleConfigListAction.ToggleSelected(config.id))
                            } else {
                                action(BattleConfigListAction.Edit(config.id))
                            }
                        },
                        onLongClick = {
                            if (!selectionMode) {
                                action(BattleConfigListAction.StartSelection(config.id))
                            }
                        },
                        isSelectionMode = selectionMode,
                        isSelected = selectionMode && config.id in selectedConfigs
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
    it: BattleConfigCore,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val name by it.name.remember()
    val materialsSet by it.materials.remember()
    val mats = materialsSet.take(3)

    // Without this, holding a list item would leave it highlighted because
    // of recomposition happening before ripple ending
    val longClickState = rememberUpdatedState(onLongClick)

    Card(
        shape = RoundedCornerShape(25),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 5.dp else 1.dp),
        modifier = Modifier
            .padding(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { longClickState.value.invoke() }
                )
                .padding(16.dp, 5.dp)
        ) {
            BattleConfigItemSelected(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            mats.forEach {
                Material(it)
            }
        }
    }
}