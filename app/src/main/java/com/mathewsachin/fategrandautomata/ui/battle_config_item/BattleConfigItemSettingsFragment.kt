package com.mathewsachin.fategrandautomata.ui.battle_config_item

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.FgaScreen
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.HeadingButton
import com.mathewsachin.fategrandautomata.ui.card_priority.getColorRes
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.pref_support.PreferredSupportViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.EditTextPreference
import com.mathewsachin.fategrandautomata.ui.prefs.Preference
import com.mathewsachin.fategrandautomata.util.nav
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.error
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BattleConfigItemSettingsFragment : Fragment() {
    @Inject
    lateinit var preferences: IPreferences

    @Inject
    lateinit var prefsCore: PrefsCore

    val vm: BattleConfigItemViewModel by viewModels()
    val supportViewModel: PreferredSupportViewModel by activityViewModels()

    val args: BattleConfigItemSettingsFragmentArgs by navArgs()

    private fun exportBattleConfig(uri: Uri?) {
        if (uri != null) {
            try {
                requireContext().contentResolver.openOutputStream(uri)?.use { outStream ->
                    vm.export(outStream)
                }
            } catch (e: Exception) {
                Timber.error(e) { "Failed to export" }

                val msg = getString(R.string.battle_config_item_export_failed)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var battleConfig: IBattleConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        battleConfig = preferences.forBattleConfig(args.key)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            val config = prefsCore.forBattleConfig(args.key)

            setContent {
                val battleConfigExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
                    exportBattleConfig(uri)
                }

                BattleConfigItemView(
                    config = config,
                    friendEntries = supportViewModel.friends,
                    onExport = { battleConfigExport.launch("${battleConfig.name}.fga") },
                    onCopy = { copy() },
                    onDelete = { deleteItem(battleConfig.id) },
                    openSkillMaker = {
                        val action = BattleConfigItemSettingsFragmentDirections
                            .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(args.key)

                        nav(action)
                    },
                    openCardPriority = {
                        val action = BattleConfigItemSettingsFragmentDirections
                            .actionBattleConfigItemSettingsFragmentToCardPriorityFragment(args.key)

                        nav(action)
                    },
                    openSpam = {
                        val action = BattleConfigItemSettingsFragmentDirections
                            .actionBattleConfigItemSettingsFragmentToSpamSettingsFragment(args.key)

                        nav(action)
                    },
                    openPreferredSupport = {
                        val action = BattleConfigItemSettingsFragmentDirections
                            .actionBattleConfigItemSettingsFragmentToPreferredSupportSettingsFragment(args.key)

                        nav(action)
                    }
                )
            }
        }

    private fun copy() {
        val guid = UUID.randomUUID().toString()
        preferences.addBattleConfig(guid)
        val newConfig = preferences.forBattleConfig(guid)

        val map = battleConfig.export()
        newConfig.import(map)
        newConfig.name = getString(R.string.battle_config_item_copy_name, newConfig.name)

        val action = BattleConfigItemSettingsFragmentDirections
            .actionBattleConfigItemSettingsFragmentSelf(guid)

        nav(action)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            if (supportViewModel.shouldExtractSupportImages) {
                performSupportImageExtraction()
            } else supportViewModel.refresh(requireContext())
        }
    }

    private suspend fun performSupportImageExtraction() {
        val msg = try {
            supportViewModel.extract(requireContext())

            getString(R.string.support_imgs_extracted)
        } catch (e: Exception) {
            getString(R.string.support_imgs_extract_failed).also { msg ->
                Timber.error(e) { msg }
            }
        }

        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun deleteItem(battleConfigKey: String) {
        preferences.removeBattleConfig(battleConfigKey)

        findNavController().popBackStack()
    }
}

@Composable
fun BattleConfigItemView(
    config: BattleConfigCore,
    friendEntries: Map<String, String>,
    onExport: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    openSkillMaker: () -> Unit,
    openCardPriority: () -> Unit,
    openSpam: () -> Unit,
    openPreferredSupport: () -> Unit,
    vm: BattleConfigItemViewModel = viewModel()
) {
    FgaScreen {
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
                                openSkillMaker = openSkillMaker
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
                                                .clickable(onClick = openSpam)
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
                                        onClick = openCardPriority
                                    )
                                }
                            }
                        }
                    }

                    item {
                        val maxSkillText by vm.maxSkillText.collectAsState("")

                        SupportGroup(
                            config = config,
                            goToPreferred = openPreferredSupport,
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
}

val CardScore.color: Color
    @Composable get() {
        // Dark colors won't be visible in dark theme
        val score = if (MaterialTheme.colors.isLight)
            this
        else CardScore(CardType, CardAffinityEnum.Resist)

        return colorResource(score.getColorRes())
    }

@Composable
fun CardPrioritySummary(cardPriority: CardPriorityPerWave) {
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