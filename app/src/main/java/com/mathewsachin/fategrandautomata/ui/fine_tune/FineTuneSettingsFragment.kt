package com.mathewsachin.fategrandautomata.ui.fine_tune

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.prefs.core.PrefsCore
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import com.mathewsachin.fategrandautomata.ui.Heading
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FineTuneSettingsFragment : Fragment() {
    val vm: FineTuneSettingsViewModel by viewModels()

    @Inject
    lateinit var prefs: PrefsCore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Heading(stringResource(R.string.p_fine_tune))

                        var selectedGroup by remember { mutableStateOf(vm.groups[0]) }

                        FineTuneGroupSelector(
                            groups = vm.groups,
                            selected = selectedGroup,
                            onSelectedChange = { selectedGroup = it }
                        )

                        Divider()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 16.dp)
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                items(selectedGroup.items) {
                                    it.FineTuneSeekBar()
                                }
                            }

                            ExtendedFloatingActionButton(
                                text = {
                                    Text(
                                        stringResource(R.string.fine_tune_menu_reset_to_defaults),
                                        color = MaterialTheme.colors.onSecondary
                                    )
                                },
                                onClick = { vm.resetAll() },
                                icon = {
                                    Icon(
                                        painterResource(R.drawable.ic_refresh),
                                        contentDescription = stringResource(R.string.fine_tune_menu_reset_to_defaults),
                                        tint = MaterialTheme.colors.onSecondary
                                    )
                                },
                                backgroundColor = MaterialTheme.colors.secondary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
}