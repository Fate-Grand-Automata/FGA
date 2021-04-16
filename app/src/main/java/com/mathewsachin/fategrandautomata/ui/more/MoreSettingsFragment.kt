package com.mathewsachin.fategrandautomata.ui.more

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.ui.FgaScreen
import com.mathewsachin.fategrandautomata.ui.GroupSelectorItem
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.util.StorageProvider
import com.mathewsachin.fategrandautomata.util.SupportImageExtractor
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.registerPersistableDirPicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.error
import javax.inject.Inject

@AndroidEntryPoint
class MoreSettingsFragment : Fragment() {
    @Inject
    lateinit var storageProvider: StorageProvider

    @Inject
    lateinit var prefs: PrefsCore

    private val storageSummary: MutableState<String?> = mutableStateOf(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaScreen {
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
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                when (selectedGroup) {
                                    MoreSettingsGroup.Battle -> {
                                        battleGroup(prefs)
                                    }
                                    MoreSettingsGroup.Storage -> {
                                        item {
                                            val summary by storageSummary
                                            var extractSummary by remember { mutableStateOf("--") }

                                            StorageGroup(
                                                directoryName = summary ?: "",
                                                onPickDirectory = { pickDir.launch(Uri.EMPTY) },
                                                extractSupportImages = {
                                                    lifecycleScope.launch {
                                                        performSupportImageExtraction {
                                                            extractSummary = it
                                                        }
                                                    }
                                                },
                                                extractSummary = extractSummary
                                            )
                                        }
                                    }
                                    MoreSettingsGroup.Advanced -> {
                                        advancedGroup(
                                            prefs,
                                            goToFineTune = {
                                                val action = MoreSettingsFragmentDirections
                                                    .actionMoreSettingsFragmentToFineTuneSettingsFragment()

                                                nav(action)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.also {
            storageSummary.value = storageProvider.rootDirName
        }

    private val pickDir = registerPersistableDirPicker {
        storageProvider.setRoot(it)

        storageSummary.value = storageProvider.rootDirName
    }

    private suspend fun performSupportImageExtraction(
        updateSummary: (String) -> Unit
    ) {
        // TODO: Translate
        updateSummary("Extracting ...")

        val msg = try {
            SupportImageExtractor(requireContext(), storageProvider).extract()

            getString(R.string.support_imgs_extracted)
        } catch (e: Exception) {
            getString(R.string.support_imgs_extract_failed).also { msg ->
                Timber.error(e) { msg }
            }
        }

        updateSummary(msg)
    }
}

@Composable
fun Int.boostItemString() = when (this) {
    -1 -> stringResource(R.string.p_boost_item_disabled)
    0 -> stringResource(R.string.p_boost_item_skip)
    else -> toString()
}

val GameServerEnum.displayStringRes
    get() = when (this) {
        GameServerEnum.En -> R.string.game_server_na
        GameServerEnum.Jp -> R.string.game_server_jp
        GameServerEnum.Cn -> R.string.game_server_cn
        GameServerEnum.Tw -> R.string.game_server_tw
        GameServerEnum.Kr -> R.string.game_server_kr
    }

enum class MoreSettingsGroup {
    Battle, Storage, Advanced;

    val displayStringRes get() = when (this) {
        Battle -> R.string.p_script_mode_battle
        Storage -> R.string.p_storage
        Advanced -> R.string.p_advanced
    }
}