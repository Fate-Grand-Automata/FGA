package com.mathewsachin.fategrandautomata.ui.prefs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.scripts.enums.SpamEnum
import com.mathewsachin.fategrandautomata.scripts.models.SkillSpamTarget
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme
import com.mathewsachin.fategrandautomata.ui.prefs.compose.SwitchPreference
import com.mathewsachin.fategrandautomata.ui.prefs.compose.listDialog
import com.mathewsachin.fategrandautomata.ui.prefs.compose.multiSelectListDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpamSettingsFragment : Fragment() {
    val args: SpamSettingsFragmentArgs by navArgs()
    val vm: SpamSettingsViewModel by viewModels()

    @Inject
    lateinit var prefsCore: PrefsCore

    override fun onPause() {
        super.onPause()

        vm.save()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            val config = prefsCore.forBattleConfig(args.key)

            setContent {
                FgaTheme {
                    ScrollableColumn {
                        config.autoChooseTarget.SwitchPreference(
                            title = stringResource(R.string.p_auto_choose_target),
                            summary = stringResource(R.string.p_spam_summary)
                        )

                        Divider()

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    "Servant:",
                                    modifier = Modifier.padding(end = 16.dp)
                                )

                                (1..vm.spamStates.size).map {
                                    val isSelected = vm.selectedServant == it - 1

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSelected) MaterialTheme.colors.secondary else Color.Transparent,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .clickable { vm.selectedServant = it - 1 }
                                            .padding(14.dp, 5.dp)
                                    ) {
                                        Text(
                                            it.toString(),
                                            color = if (isSelected) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onSurface
                                        )
                                    }
                                }
                            }

                            val selectedConfig = vm.spamStates[vm.selectedServant]

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("NP:")

                                var selectedSpamMode by selectedConfig.np.spamMode
                                var selectedWaves by selectedConfig.np.waves

                                SelectSpamMode(
                                    selected = selectedSpamMode,
                                    onSelectChange = { selectedSpamMode = it },
                                    modifier = Modifier.weight(1f)
                                )

                                if (selectedSpamMode != SpamEnum.None) {
                                    SelectWaves(
                                        selected = selectedWaves,
                                        onSelectChange = { selectedWaves = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            selectedConfig.skills.mapIndexed { index, skillConfig ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("S${index + 1}:")

                                    var selectedSpamMode by skillConfig.spamMode
                                    var selectedTarget by skillConfig.target
                                    var selectedWaves by skillConfig.waves

                                    SelectSpamMode(
                                        selected = selectedSpamMode,
                                        onSelectChange = { selectedSpamMode = it },
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (selectedSpamMode != SpamEnum.None) {
                                        SelectTarget(
                                            selected = selectedTarget,
                                            onSelectChange = { selectedTarget = it },
                                            modifier = Modifier.weight(1f)
                                        )

                                        SelectWaves(
                                            selected = selectedWaves,
                                            onSelectChange = { selectedWaves = it },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun SelectSpamMode(
    selected: SpamEnum,
    onSelectChange: (SpamEnum) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialog = listDialog(
        selected = selected,
        selectedChange = onSelectChange,
        entries = SpamEnum.values().associateWith { it.toString() },
        title = "Spam mode"
    )

    ListItem(
        text = { Text("Mode") },
        secondaryText = { Text(selected.toString()) },
        modifier = modifier
            .clickable { dialog.show() }
    )
}

@Composable
fun SelectTarget(
    selected: SkillSpamTarget,
    onSelectChange: (SkillSpamTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialog = listDialog(
        selected = selected,
        selectedChange = onSelectChange,
        entries = SkillSpamTarget.values().associateWith { it.toString() },
        title = "Target"
    )

    ListItem(
        text = { Text("Target") },
        secondaryText = { Text(selected.toString()) },
        modifier = modifier
            .clickable { dialog.show() }
    )
}

@Composable
fun SelectWaves(
    selected: Set<Int>,
    onSelectChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialog = multiSelectListDialog(
        selected = selected,
        selectedChange = onSelectChange,
        entries = (1..3).associateWith { "Wave $it" },
        title = "Waves"
    )

    ListItem(
        text = { Text("Waves") },
        secondaryText = { Text(selected.joinToString()) },
        modifier = modifier
            .clickable { dialog.show() }
    )
}