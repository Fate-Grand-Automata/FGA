package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.ui.prefs.compose.FgaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SkillMakerAtkFragment : Fragment() {
    val viewModel: SkillMakerViewModel by activityViewModels()

    @Inject
    lateinit var prefs: IPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                FgaTheme {
                    AtkScreen(
                        onNextWave = { goToNextStage(it) },
                        onNextTurn = { goToNextTurn(it) }
                    )
                }
            }
        }

    private fun goBack() {
        findNavController().popBackStack()
    }

    fun goToNextStage(atk: AutoSkillAction.Atk) {
        viewModel.nextStage(atk)

        goBack()
    }

    fun goToNextTurn(atk: AutoSkillAction.Atk) {
        viewModel.nextTurn(atk)

        goBack()
    }
}

@Composable
fun AtkScreen(
    onNextWave: (AutoSkillAction.Atk) -> Unit,
    onNextTurn: (AutoSkillAction.Atk) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.skill_maker_atk_header),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        var npSequence by savedInstanceState { "" }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            (1..3).map {
                val isSelected = it.toString() in npSequence

                val selectedColor = when (it) {
                    1 -> R.color.colorServant1
                    2 -> R.color.colorServant2
                    3 -> R.color.colorServant3
                    else -> R.color.colorAccent
                }

                val onClick = {
                    npSequence =
                        if (isSelected)
                            npSequence.filter { m -> m.toString() != it.toString() }
                        else npSequence + it
                }

                Surface(
                    elevation = 5.dp,
                    color =
                    if (isSelected)
                        colorResource(selectedColor)
                    else MaterialTheme.colors.surface,
                    modifier = Modifier
                        .padding(5.dp)
                        .clickable { onClick() }
                ) {
                    Text(
                        stringResource(R.string.skill_maker_atk_servant_np, it),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        color =
                        if (isSelected)
                            Color.White
                        else Color.Unspecified
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            var cardsBeforeNp by savedInstanceState { 0 }

            Column {
                Text(stringResource(R.string.skill_maker_atk_cards_before_np))

                Row {
                    (0..2).map {
                        val isSelected = cardsBeforeNp == it

                        Surface(
                            elevation = 5.dp,
                            color =
                            if (isSelected)
                                colorResource(R.color.colorAccent)
                            else MaterialTheme.colors.surface,
                            modifier = Modifier
                                .padding(5.dp)
                                .clickable { cardsBeforeNp = it }
                        ) {
                            Text(
                                it.toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp, 10.dp),
                                color =
                                if (isSelected)
                                    Color.White
                                else Color.Unspecified
                            )
                        }
                    }
                }
            }

            fun npSet() =
                npSequence
                    .mapNotNull {
                        when (it) {
                            '1' -> CommandCard.NP.A
                            '2' -> CommandCard.NP.B
                            '3' -> CommandCard.NP.C
                            else -> null
                        }
                    }
                    .toSet()

            Row {
                Button(
                    onClick = { onNextTurn(AutoSkillAction.Atk(npSet(), cardsBeforeNp)) },
                    modifier = Modifier
                        .padding(end = 16.dp)
                ) {
                    Text(
                        stringResource(R.string.skill_maker_atk_next_turn),
                        textAlign = TextAlign.Center
                    )
                }

                ButtonWithIcon(
                    text = R.string.skill_maker_atk_next_wave,
                    icon = R.drawable.ic_fast_forward,
                    onClick = { onNextWave(AutoSkillAction.Atk(npSet(), cardsBeforeNp)) }
                )
            }
        }
    }
}

@Composable
fun ButtonWithIcon(
    @StringRes text: Int,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    ) {
        Row {
            Icon(
                vectorResource(icon),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
            )

            Text(stringResource(text))
        }
    }
}