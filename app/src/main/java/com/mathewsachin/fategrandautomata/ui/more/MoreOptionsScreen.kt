package com.mathewsachin.fategrandautomata.ui.more

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.ui.GroupSelectorItem
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.util.OpenDocTreePersistable

@Composable
fun MoreOptionsScreen(
    vm: MoreOptionsViewModel = viewModel(),
    navigateToFineTune: () -> Unit
) {
    val pickDirectory = rememberLauncherForActivityResult(OpenDocTreePersistable()) {
        vm.pickedDirectory(it)
    }

    MoreOptionsContent(
        vm = vm,
        goToFineTune = navigateToFineTune,
        pickDirectory = { pickDirectory.launch(Uri.EMPTY) }
    )
}

@Composable
private fun MoreOptionsContent(
    vm: MoreOptionsViewModel,
    goToFineTune: () -> Unit,
    pickDirectory: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
            var selectedGroup by rememberSaveable { mutableStateOf(MoreSettingsGroup.Battle) }

            Heading(stringResource(R.string.p_more_options)) {
                items(MoreSettingsGroup.values().toList()) {
                    GroupSelectorItem(
                        item = stringResource(it.displayStringRes),
                        isSelected = selectedGroup == it,
                        onSelect = { selectedGroup = it }
                    )
                }
            }

            Divider()

            Card(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedGroup
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        when (it) {
                            MoreSettingsGroup.Battle -> {
                                battleGroup(vm.prefsCore)
                            }
                            MoreSettingsGroup.Storage -> {
                                item {
                                    val summary by vm.storageSummary
                                    val extractSummary by vm.extractSummary
                                    val context = LocalContext.current

                                    StorageGroup(
                                        directoryName = summary ?: "",
                                        onPickDirectory = pickDirectory,
                                        extractSupportImages = { vm.performSupportImageExtraction(context) },
                                        extractSummary = extractSummary
                                    )
                                }
                            }
                            MoreSettingsGroup.Advanced -> {
                                advancedGroup(
                                    vm.prefsCore,
                                    goToFineTune = goToFineTune
                                )
                            }
                        }
                    }
                }
            }
        }
}

private enum class MoreSettingsGroup {
    Battle, Storage, Advanced;

    val displayStringRes get() = when (this) {
        Battle -> R.string.p_script_mode_battle
        Storage -> R.string.p_storage
        Advanced -> R.string.p_advanced
    }
}