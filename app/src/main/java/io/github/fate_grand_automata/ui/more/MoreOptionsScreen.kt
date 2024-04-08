package io.github.fate_grand_automata.ui.more

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.ui.Heading
import io.github.fate_grand_automata.ui.Tabbed
import io.github.fate_grand_automata.util.OpenDocTreePersistable

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
        Heading(stringResource(R.string.p_more_options))

        Tabbed(
            items = MoreSettingsGroup.entries.toList(),
            heading = { Text(stringResource(it.stringRes)) },
            content = {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
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
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

private enum class MoreSettingsGroup {
    Battle, Storage, Advanced;

    val stringRes
        get() = when (this) {
            Battle -> R.string.p_script_mode_battle
            Storage -> R.string.p_storage
            Advanced -> R.string.p_advanced
        }
}