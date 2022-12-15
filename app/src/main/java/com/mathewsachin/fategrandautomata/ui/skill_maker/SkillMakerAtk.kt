package com.mathewsachin.fategrandautomata.ui.skill_maker

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import com.mathewsachin.fategrandautomata.ui.VectorIcon
import com.mathewsachin.fategrandautomata.ui.icon

@Composable
private fun SelectNps(
    npSequence: String,
    onNpSequenceChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
                onNpSequenceChange(
                    if (isSelected)
                        npSequence.filter { m -> m.toString() != it.toString() }
                    else npSequence + it
                )
            }

            Surface(
                tonalElevation = 5.dp,
                shape = MaterialTheme.shapes.medium,
                color =
                if (isSelected)
                    colorResource(selectedColor)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(5.dp),
                onClick = onClick
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
}

@Composable
private fun CardsBeforeNp(
    cardsBeforeNp: Int,
    onCardsBeforeNpChange: (Int) -> Unit
) {
    Column {
        Text(stringResource(R.string.skill_maker_atk_cards_before_np))

        Row {
            (0..2).map {
                val isSelected = cardsBeforeNp == it

                Surface(
                    tonalElevation = 5.dp,
                    shape = MaterialTheme.shapes.medium,
                    color =
                    if (isSelected)
                        colorResource(R.color.colorAccent)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(5.dp),
                    onClick = { onCardsBeforeNpChange(it) }
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
}

@Composable
fun SkillMakerAtk(
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

        var npSequence by rememberSaveable { mutableStateOf("") }

        SelectNps(
            npSequence = npSequence,
            onNpSequenceChange = { npSequence = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            var cardsBeforeNp by rememberSaveable { mutableStateOf(0) }

            CardsBeforeNp(
                cardsBeforeNp = cardsBeforeNp,
                onCardsBeforeNpChange = { cardsBeforeNp = it }
            )

            Row {
                Button(
                    onClick = { onNextTurn(makeAtkAction(npSequence, cardsBeforeNp)) },
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
                    icon = icon(Icons.Default.FastForward),
                    onClick = { onNextWave(makeAtkAction(npSequence, cardsBeforeNp)) }
                )
            }
        }
    }
}

private fun makeAtkAction(npSequence: String, cardsBeforeNp: Int): AutoSkillAction.Atk {
    val npSet = npSequence
        .mapNotNull {
            when (it) {
                '1' -> CommandCard.NP.A
                '2' -> CommandCard.NP.B
                '3' -> CommandCard.NP.C
                else -> null
            }
        }
        .toSet()

    return AutoSkillAction.Atk(npSet, cardsBeforeNp)
}

@Composable
fun ButtonWithIcon(
    @StringRes text: Int,
    icon: VectorIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            icon.asPainter(),
            contentDescription = "button icon",
            modifier = Modifier
                .padding(end = 16.dp)
                .size(20.dp)
        )

        Text(stringResource(text))
    }
}