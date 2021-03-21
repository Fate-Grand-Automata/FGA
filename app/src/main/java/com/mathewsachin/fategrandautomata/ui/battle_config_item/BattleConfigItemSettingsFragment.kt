package com.mathewsachin.fategrandautomata.ui.battle_config_item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.BattleConfigCore
import com.mathewsachin.fategrandautomata.prefs.core.Pref
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.CardAffinityEnum
import com.mathewsachin.fategrandautomata.scripts.enums.MaterialEnum
import com.mathewsachin.fategrandautomata.scripts.models.CardPriorityPerWave
import com.mathewsachin.fategrandautomata.scripts.models.CardScore
import com.mathewsachin.fategrandautomata.scripts.prefs.IBattleConfig
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import com.mathewsachin.fategrandautomata.ui.Heading
import com.mathewsachin.fategrandautomata.ui.HeadingButton
import com.mathewsachin.fategrandautomata.ui.card_priority.getColorRes
import com.mathewsachin.fategrandautomata.ui.icon
import com.mathewsachin.fategrandautomata.ui.pref_support.PreferredSupportViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.*
import com.mathewsachin.fategrandautomata.util.drawable
import com.mathewsachin.fategrandautomata.util.nav
import com.mathewsachin.fategrandautomata.util.stringRes
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
                FgaTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Heading(stringResource(R.string.p_nav_battle_config_edit)) {
                            item {
                                HeadingButton(
                                    text = stringResource(R.string.battle_config_item_export),
                                    onClick = {
                                        battleConfigExport.launch("${battleConfig.name}.fga")
                                    }
                                )
                            }

                            item {
                                HeadingButton(
                                    text = stringResource(R.string.battle_config_item_copy),
                                    icon = icon(R.drawable.ic_copy),
                                    onClick = { copy() }
                                )
                            }

                            item {
                                HeadingButton(
                                    text = stringResource(R.string.battle_config_item_delete),
                                    color = MaterialTheme.colors.error,
                                    icon = icon(R.drawable.ic_delete),
                                    onClick = {
                                        AlertDialog.Builder(requireContext())
                                            .setMessage(R.string.battle_config_item_delete_confirm_message)
                                            .setTitle(R.string.battle_config_item_delete_confirm_title)
                                            .setPositiveButton(R.string.battle_config_item_delete_confirm_ok) { _, _ -> deleteItem(args.key) }
                                            .setNegativeButton(android.R.string.cancel, null)
                                            .show()
                                    }
                                )
                            }

                            item {
                                HeadingButton(
                                    text = stringResource(R.string.support_menu_extract_default_support_images),
                                    onClick = {
                                        lifecycleScope.launch {
                                            performSupportImageExtraction()
                                        }
                                    }
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        config.name.EditTextPreference(
                                            title = stringResource(R.string.p_battle_config_name),
                                            validate = { it.isNotBlank() },
                                            singleLine = true
                                        )
                                    }

                                    PartySelection(config)
                                }

                                Divider()
                            }

                            item {
                                SkillCommandGroup(
                                    config = config,
                                    vm = vm,
                                    openSkillMaker = {
                                        val action = BattleConfigItemSettingsFragmentDirections
                                            .actionBattleConfigItemSettingsFragmentToBattleConfigMakerActivity(args.key)

                                        nav(action)
                                    }
                                )

                                Divider()
                            }

                            item {
                                config.materials.Materials()

                                Divider()
                            }

                            item {
                                config.notes.EditTextPreference(
                                    title = stringResource(R.string.p_battle_config_notes)
                                )

                                Divider()
                            }

                            item {
                                val cardPriority by vm.cardPriority.collectAsState(null)

                                cardPriority?.let {
                                    Preference(
                                        title = { Text(stringResource(R.string.p_battle_config_card_priority)) },
                                        summary = { CardPrioritySummary(it) },
                                        onClick = {
                                            val action = BattleConfigItemSettingsFragmentDirections
                                                .actionBattleConfigItemSettingsFragmentToCardPriorityFragment(args.key)

                                            nav(action)
                                        }
                                    )
                                }

                                Divider()
                            }

                            item {
                                Preference(
                                    title = stringResource(R.string.p_spam_spam),
                                    onClick = {
                                        val action = BattleConfigItemSettingsFragmentDirections
                                            .actionBattleConfigItemSettingsFragmentToSpamSettingsFragment(args.key)

                                        nav(action)
                                    }
                                )

                                Divider()
                            }

                            item {
                                val maxSkillText by vm.maxSkillText.collectAsState("")

                                SupportGroup(
                                    config = config,
                                    goToPreferred = {
                                        val action = BattleConfigItemSettingsFragmentDirections
                                            .actionBattleConfigItemSettingsFragmentToPreferredSupportSettingsFragment(args.key)

                                        nav(action)
                                    },
                                    maxSkillText = maxSkillText,
                                    friendEntries = supportViewModel.friends
                                )

                                Divider()
                            }

                            item {
                                ShuffleCardsGroup(config)
                            }
                        }
                    }
                }
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
fun Pref<Set<MaterialEnum>>.Materials() {
    var selected by remember()

    val title = stringResource(R.string.p_mats)
    val entries = MaterialEnum.values()
        .associateWith { stringResource(it.stringRes) }

    val dialog = multiSelectListDialog(
        selected = selected,
        selectedChange = { selected = it },
        entries = entries,
        title = title
    )

    ListItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    MaterialsSummary(materials = selected.toList())
                }
            }
        },
        modifier = Modifier
            .clickable { dialog.show() }
    )
}

@Composable
fun MaterialsSummary(materials: List<MaterialEnum>) {
    if (materials.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                shape = RoundedCornerShape(50)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(7.dp, 5.dp)
                ) {
                    items(materials) { mat ->
                        Image(
                            painterResource(mat.drawable),
                            contentDescription = stringResource(mat.stringRes),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(0.5.dp, MaterialTheme.colors.onSurface, CircleShape)
                                .alpha(0.8f)
                        )
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

@Composable
fun PartySelection(config: BattleConfigCore) {
    var party by config.party.remember()

    val dialog = listDialog(
        selected = party,
        selectedChange = { party = it },
        entries = (-1..9)
            .associateWith {
                when (it) {
                    -1 -> stringResource(R.string.p_not_set)
                    else -> stringResource(R.string.p_party_number, it + 1)
                }
            },
        title = stringResource(R.string.p_battle_config_party)
    )

    Column(
        modifier = Modifier
            .clickable { dialog.show() }
            .padding(16.dp, 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.p_battle_config_party)
                .toUpperCase(Locale.ROOT),
            style = MaterialTheme.typography.caption
        )

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(if (party == -1) "-" else (party + 1).toString())
        }
    }
}