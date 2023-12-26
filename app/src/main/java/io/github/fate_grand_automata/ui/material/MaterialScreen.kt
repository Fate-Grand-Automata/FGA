package io.github.fate_grand_automata.ui.material

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.MaterialEnum
import io.github.fate_grand_automata.scripts.enums.MaterialRarity
import io.github.fate_grand_automata.util.drawable
import io.github.fate_grand_automata.util.stringRes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MaterialScreen(
    windowSizeClass: WindowSizeClass,
    vm: MaterialViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val selectedMaterials by vm.selectedMaterials.collectAsState(emptyList())

    val enableReset by vm.enableReset.collectAsState(false)

    val materialListTracker by vm.materialsListTracker.collectAsState(emptyList())

    val query by vm.searchQuery.collectAsState("")

    val pager = rememberPagerState {
        MaterialRarity.entries.size
    }

    var entries by remember {
        mutableStateOf(emptyList<MaterialEnum>())
    }
    var currentMats by remember {
        mutableStateOf(emptyList<MaterialEnum>())
    }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = selectedMaterials, block = {
        entries = MaterialEnum.entries.toList().filter {
            context.getString(it.stringRes).contains(query, ignoreCase = true)
        }.sortedByDescending { shown ->
            shown in selectedMaterials
        }
        currentMats = entries.filter {
            it.rarity == MaterialRarity.entries[pager.currentPage]
        }
        focusManager.clearFocus()
    })

    LaunchedEffect(key1 = query, block = {
        entries = MaterialEnum.entries.toList().filter {
            context.getString(it.stringRes).contains(query, ignoreCase = true)
        }.sortedByDescending { shown ->
            shown in selectedMaterials
        }
        currentMats = entries.filter {
            it.rarity == MaterialRarity.entries[pager.currentPage]
        }
    })
    LaunchedEffect(key1 = pager, block = {
        snapshotFlow { pager.currentPage }.collectLatest { page ->
            currentMats = entries.filter {
                it.rarity == MaterialRarity.entries[page]
            }
            focusManager.clearFocus()
        }
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.p_mats),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    MatActionButtons(
                        windowSizeClass = windowSizeClass,
                        enableReset = enableReset,
                        materialListTracker = materialListTracker,
                        selectedMaterials = selectedMaterials,
                        onReset = { vm.reset() },
                        onClear = { vm.removeAllMaterials() },
                        onUndo = { vm.undo() }
                    )
                }
            )
        },
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            TextField(
                value = query,
                onValueChange = {
                    vm.onQueryUpdated(it)
                },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            vm.onQueryUpdated("")
                            focusManager.clearFocus()
                        },
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                ),
                placeholder = {
                    Text(
                        stringResource(R.string.p_material_search),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SelectedMaterialRow(
                    modifier = Modifier.weight(1f),
                    selectedMaterials = selectedMaterials,
                    onRemoved = { vm.removeMaterial(it) }
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                MaterialTabRow(
                    windowSizeClass = windowSizeClass, pager, selectedMaterials, query, entries
                )
                HorizontalPager(state = pager) { _ ->
                    MaterialPager(
                        windowSizeClass,
                        currentMats,
                        selectedMaterials,
                        onClick = { mat ->
                            if (mat in selectedMaterials) {
                                vm.removeMaterial(mat)
                            } else {
                                vm.addMaterial(mat)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatActionButtons(
    windowSizeClass: WindowSizeClass,
    enableReset: Boolean,
    materialListTracker: List<UndoTracker>,
    selectedMaterials: List<MaterialEnum>,
    onReset: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit
) {
    ResetButton(
        windowSizeClass,
        enableReset = enableReset,
        onReset = onReset
    )
    UndoButton(
        windowSizeClass,
        materialListTracker,
        onUndo = onUndo
    )
    ClearButton(
        windowSizeClass,
        selectedMaterials,
        onClear = onClear
    )
}

@Composable
private fun SelectedMaterialRow(
    modifier: Modifier = Modifier,
    selectedMaterials: List<MaterialEnum>,
    onRemoved: (MaterialEnum) -> Unit
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(selectedMaterials) { mat ->
            MaterialImage(
                mat = mat,
                size = 40.dp,
                onClick = {
                    onRemoved(mat)
                }
            )
        }
    }
}

@Composable
private fun ClearButton(
    windowSizeClass: WindowSizeClass,
    selectedMaterials: List<MaterialEnum>,
    onClear: () -> Unit
) {
    iconOrTextVariableButton(
        windowSizeClass = windowSizeClass,
        enabled = selectedMaterials.isNotEmpty(),
        icon = Icons.Default.Clear,
        text = stringResource(R.string.skill_maker_main_clear).uppercase(),
        action = onClear
    )
}

@Composable
private fun UndoButton(
    windowSizeClass: WindowSizeClass,
    materialListTracker: List<UndoTracker>,
    onUndo: () -> Unit
) {
    iconOrTextVariableButton(
        windowSizeClass = windowSizeClass,
        enabled = materialListTracker.isNotEmpty(),
        icon = Icons.Default.Undo,
        text = stringResource(R.string.skill_maker_main_undo).uppercase(),
        action = onUndo
    )

}

@Composable
private fun ResetButton(
    windowSizeClass: WindowSizeClass,
    enableReset: Boolean,
    onReset: () -> Unit
) {
    iconOrTextVariableButton(
        windowSizeClass = windowSizeClass,
        enabled = enableReset,
        icon = Icons.Default.RestartAlt,
        text = stringResource(R.string.reset).uppercase(),
        action = onReset
    )
}

@Composable
private fun iconOrTextVariableButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    windowSizeClass: WindowSizeClass,
    icon: ImageVector,
    text: String,
    action: () -> Unit,
) {
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        PlainTooltipBox(
            tooltip = {
                Text(text = text)
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .tooltipAnchor()
            ) {
                IconButton(
                    onClick = action,
                    enabled = enabled,
                    modifier = modifier
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = text)
                }
            }
        }
    } else {
        TextButton(
            onClick = action,
            enabled = enabled,
            modifier = modifier
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = text,
            )
        }
    }
}

@Composable
private fun MaterialTabRow(
    windowSizeClass: WindowSizeClass,
    pager: PagerState,
    selectedMaterials: List<MaterialEnum>,
    query: String,
    entries: List<MaterialEnum>
) {
    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pager.currentPage,
    ) {
        MaterialRarity.entries.forEachIndexed { index, rarity ->
            Tab(
                selected = index == pager.currentPage,
                onClick = {
                    scope.launch {
                        pager.animateScrollToPage(index)
                    }
                },
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedMatCount = selectedMaterials.count { it.rarity == rarity }
                    val isSpaceOrNewLine = when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Compact -> "\n"
                        else -> " "
                    }
                    Text(
                        when {
                            selectedMatCount > 0 -> "${rarity.name}$isSpaceOrNewLine($selectedMatCount)"
                            else -> rarity.name
                        }.uppercase(),
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .alignByBaseline(),
                        textAlign = TextAlign.Center
                    )
                    if (query.isNotEmpty()) {
                        val queryCount = entries.count { it.rarity == rarity }
                        if (queryCount > 0) {
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))

                            Card(
                                modifier = Modifier
                                    .alignByBaseline()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                colors = cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(25)
                            ) {
                                Text(
                                    "$queryCount",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(2.dp, 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialPager(
    windowSizeClass: WindowSizeClass,
    currentMats: List<MaterialEnum>,
    selectedMaterials: List<MaterialEnum>,
    onClick: (MaterialEnum) -> Unit
) {

    LazyVerticalGrid(
        columns = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Medium -> GridCells.Fixed(2)
            WindowWidthSizeClass.Expanded -> GridCells.Fixed(3)
            else -> GridCells.Fixed(1)
        },
        contentPadding = PaddingValues(16.dp, 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        currentMats.forEach { mat ->
            item(
                mat,
                span = {
                    GridItemSpan(1)
                }
            ) {
                ListItem(
                    leadingContent = {
                        MaterialImage(mat = mat)
                    },
                    headlineContent = {
                        Text(text = stringResource(id = mat.stringRes))
                    },
                    trailingContent = {
                        if (mat in selectedMaterials) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = when (mat in selectedMaterials) {
                            true -> MaterialTheme.colorScheme.primaryContainer
                            false -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .clip(
                            RoundedCornerShape(12)
                        )
                        .clickable(
                            onClick = {
                                onClick(mat)
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Composable
private fun MaterialImage(
    modifier: Modifier = Modifier,
    mat: MaterialEnum,
    size: Dp = 20.dp,
    onClick: () -> Unit = {}
) {
    Image(
        painterResource(mat.drawable),
        contentDescription = stringResource(mat.stringRes),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .padding(3.dp)
            .size(size)
            .clip(CircleShape)
            .border(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            .alpha(0.8f)
            .clickable(
                onClick = onClick
            )
    )
}