package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.*
import com.google.gson.Gson
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.*
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.compose.*
import com.mathewsachin.fategrandautomata.util.*
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

    val battleConfigExport = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            try {
                val values = preferences.forBattleConfig(args.key).export()
                val gson = Gson()
                val json = gson.toJson(values)

                requireContext().contentResolver.openOutputStream(uri)?.use { outStream ->
                    outStream.writer().use { it.write(json) }
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
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            val config = prefsCore.forBattleConfig(args.key)

            setContent {
                FgaTheme {
                    ScrollableColumn {
                        config.name.EditTextPreference(
                            title = stringResource(R.string.p_battle_config_name),
                            icon = vectorResource(R.drawable.ic_text_short)
                        )

                        // TODO: Allow going from Text Box to Skill Maker
                        val cmd by config.skillCommand.collect()
                        val cmdDialog = editTextDialog(
                            title = stringResource(R.string.p_battle_config_cmd),
                            value = cmd,
                            valueChange = { config.skillCommand.set(it) }
                        )

                        Preference(
                            title = stringResource(R.string.p_battle_config_cmd),
                            icon = vectorResource(R.drawable.ic_terminal),
                            summary = cmd,
                            onClick = {
                                if (!prefsCore.showTextBoxForSkillCmd.get()) {
                                    val action = BattleConfigItemSettingsFragmentDirections
                                        .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(args.key)

                                    nav(action)
                                } else cmdDialog.show()
                            }
                        )

                        Divider()

                        config.notes.EditTextPreference(
                            title = stringResource(R.string.p_battle_config_notes),
                            icon = vectorResource(R.drawable.ic_note)
                        )

                        Divider()

                        val cardPriority by config.cardPriority.collect()

                        Preference(
                            title = stringResource(R.string.p_battle_config_card_priority),
                            icon = vectorResource(R.drawable.ic_sort),
                            summary = cardPriority,
                            onClick = {
                                val action = BattleConfigItemSettingsFragmentDirections
                                    .actionBattleConfigItemSettingsFragmentToCardPriorityFragment(args.key)

                                nav(action)
                            }
                        )

                        Divider()

                        Row {
                            Box(modifier = Modifier.weight(1f)) {
                                config.party.ListPreference(
                                    title = stringResource(R.string.p_battle_config_party),
                                    //icon = vectorResource(R.drawable.ic_flag),
                                    entries = (-1..9).associateWith { it.partyString }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                config.materials.MultiSelectListPreference(
                                    title = stringResource(R.string.p_mats),
                                    //icon = vectorResource(R.drawable.ic_fang),
                                    entries = MaterialEnum.values()
                                        .associate { it.name to getString(it.stringRes) }
                                )
                            }
                        }

                        Divider()

                        PreferenceGroup(title = stringResource(R.string.p_battle_config_support)) {
                            config.support.supportClass.ListPreference(
                                title = stringResource(R.string.p_battle_config_support_class),
                                //icon = vectorResource(R.drawable.ic_diamond),
                                entries = SupportClass.values()
                                    .associateWith { getString(it.stringRes) }
                            )

                            val supportMode by config.support.selectionMode.collect()
                            val preferredMode = supportMode == SupportSelectionModeEnum.Preferred
                            val friendMode = supportMode == SupportSelectionModeEnum.Friend

                            Row {
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    config.support.selectionMode.ListPreference(
                                        title = stringResource(R.string.p_battle_config_support_selection_mode),
                                        //icon = vectorResource(R.drawable.ic_dots_vertical),
                                        entries = SupportSelectionModeEnum.values()
                                            .associateWith { getString(it.stringRes) }
                                    )
                                }

                                if (preferredMode || friendMode) {
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        config.support.fallbackTo.ListPreference(
                                            title = stringResource(R.string.p_battle_config_support_fallback_selection_mode),
                                            entries = listOf(
                                                SupportSelectionModeEnum.First,
                                                SupportSelectionModeEnum.Manual
                                            ).associateWith { getString(it.stringRes) }
                                        )
                                    }
                                }
                            }

                            if (preferredMode) {
                                val preferredSummary by vm.preferredMessage.collectAsState("")

                                Preference(
                                    title = stringResource(R.string.p_support_mode_preferred),
                                    //icon = vectorResource(R.drawable.ic_card),
                                    summary = preferredSummary,
                                    onClick = {
                                        val action = BattleConfigItemSettingsFragmentDirections
                                            .actionBattleConfigItemSettingsFragmentToPreferredSupportSettingsFragment(args.key)

                                        nav(action)
                                    }
                                )
                            }

                            if (friendMode) {
                                config.support.friendNames.SupportSelectPreference(
                                    title = stringResource(R.string.p_battle_config_support_friend_names),
                                    //icon = vectorResource(R.drawable.ic_friend),
                                    entries = supportViewModel.friends
                                )
                            }
                        }

                        Divider()

                        PreferenceGroup(title = stringResource(R.string.p_spam_spam)) {
                            Text(
                                stringResource(R.string.p_spam_summary),
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                            )

                            config.autoChooseTarget.SwitchPreference(
                                title = stringResource(R.string.p_auto_choose_target),
                                //icon = vectorResource(R.drawable.ic_target)
                            )

                            Row {
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    config.npSpam.ListPreference(
                                        title = stringResource(R.string.p_spam_np),
                                        //icon = vectorResource(R.drawable.ic_star),
                                        entries = SpamEnum.values()
                                            .associateWith { getString(it.stringRes) }
                                    )
                                }

                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    config.skillSpam.ListPreference(
                                        title = stringResource(R.string.p_spam_skill),
                                        //icon = vectorResource(R.drawable.ic_wand),
                                        entries = SpamEnum.values()
                                            .associateWith { getString(it.stringRes) }
                                    )
                                }
                            }
                        }

                        Divider()

                        PreferenceGroup(title = stringResource(R.string.p_shuffle_cards)) {
                            Row {
                                Box(modifier = Modifier.weight(1f)) {
                                    config.shuffleCards.ListPreference(
                                        title = stringResource(R.string.p_shuffle_cards_when),
                                        //icon = vectorResource(R.drawable.ic_refresh),
                                        entries = ShuffleCardsEnum.values()
                                            .associateWith { getString(it.stringRes) }
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    config.shuffleCardsWave.StepperPreference(
                                        title = stringResource(R.string.p_shuffle_cards_wave),
                                        //icon = vectorResource(R.drawable.ic_counter),
                                        valueRange = 1..3
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    val Int.partyString get() = when (this) {
        -1 -> getString(R.string.p_not_set)
        else -> getString(R.string.p_party_number, this + 1)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.battle_config_item_menu, menu)
        inflater.inflate(R.menu.support_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_support_extract_defaults -> {
                lifecycleScope.launch {
                    performSupportImageExtraction()
                }
                true
            }
            R.id.action_battle_config_delete -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.battle_config_item_delete_confirm_message)
                    .setTitle(R.string.battle_config_item_delete_confirm_title)
                    .setPositiveButton(R.string.battle_config_item_delete_confirm_ok) { _, _ -> deleteItem(args.key) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_battle_config_export -> {
                battleConfigExport.launch("${battleConfig.name}.fga")
                true
            }
            R.id.action_battle_config_copy -> {
                val guid = UUID.randomUUID().toString()
                preferences.addBattleConfig(guid)
                val newConfig = preferences.forBattleConfig(guid)

                val map = battleConfig.export()
                newConfig.import(map)
                newConfig.name = getString(R.string.battle_config_item_copy_name, newConfig.name)

                val action = BattleConfigItemSettingsFragmentDirections
                    .actionBattleConfigItemSettingsFragmentSelf(guid)

                nav(action)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun deleteItem(battleConfigKey: String) {
        preferences.removeBattleConfig(battleConfigKey)

        findNavController().popBackStack()
    }
}