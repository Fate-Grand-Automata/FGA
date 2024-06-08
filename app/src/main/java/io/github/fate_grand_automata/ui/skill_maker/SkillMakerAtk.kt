package io.github.fate_grand_automata.ui.skill_maker

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.CommandCard
import io.github.fate_grand_automata.ui.FGATitle
import io.github.fate_grand_automata.ui.VectorIcon
import io.github.fate_grand_automata.ui.icon

@Composable
private fun SelectNps(
    numberOfCardsSelected: Int,
    npSequence: String,
    onNpSequenceChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val numberOfNPs = (1..3).count { it.toString() in npSequence }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        (1..3).map { servantNumber ->
            val isSelected = servantNumber.toString() in npSequence

            val canSelect = numberOfCardsSelected + numberOfNPs + 1 <= 3

            val selectedColor = when (npSequence.indexOf("$servantNumber")) {
                0 -> R.color.colorServant1
                1 -> R.color.colorServant2
                2 -> R.color.colorServant3
                else -> R.color.colorAccent
            }

            val animatedColorState by animateColorAsState(
                targetValue = when {
                    isSelected -> colorResource(selectedColor)
                    !canSelect -> MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "Add animation to the changing of color"
            )

            Surface(
                tonalElevation = 5.dp,
                shape = MaterialTheme.shapes.medium,
                color = animatedColorState,
                modifier = Modifier
                    .padding(5.dp),
                onClick = {
                    val newNpSequence = when {
                        canSelect -> {
                            if (isSelected) {
                                npSequence.filter { m -> m.toString() != servantNumber.toString() }
                            } else {
                                npSequence + servantNumber
                            }
                        }

                        isSelected -> {
                            npSequence.filter { m -> m.toString() != servantNumber.toString() }
                        }

                        else -> {
                            npSequence.dropLast(1) + servantNumber
                        }
                    }
                    onNpSequenceChange(newNpSequence)


                }
            ) {
                Text(
                    stringResource(R.string.skill_maker_atk_servant_np, servantNumber),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    color = when {
                        isSelected -> Color.White
                        !canSelect -> Color.Unspecified.copy(alpha = 0.3f)
                        else -> Color.Unspecified
                    }

                )
            }
        }
    }
}

@Composable
private fun CardsBeforeNp(
    modifier: Modifier = Modifier,
    numberOfNpSelected: Int,
    cardsBeforeNp: Int,
    onCardsBeforeNpChange: (Int) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(stringResource(R.string.skill_maker_atk_cards_before_np))

        Row {
            (0..2).map { cardNumber ->
                val isSelected = cardsBeforeNp == cardNumber

                val canSelect = numberOfNpSelected + cardNumber <= 3

                val animatedColorState by animateColorAsState(
                    targetValue = when {
                        isSelected -> colorResource(R.color.colorAccent)
                        !canSelect -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "Add animation to the changing of color"
                )

                Surface(
                    tonalElevation = 5.dp,
                    shape = MaterialTheme.shapes.medium,
                    color = animatedColorState,
                    modifier = Modifier
                        .padding(5.dp),
                    enabled = canSelect,
                    onClick = {
                        if (canSelect) {
                            onCardsBeforeNpChange(cardNumber)
                        }
                    }
                ) {
                    Text(
                        cardNumber.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp, 10.dp),
                        color = when {
                            isSelected -> Color.White
                            !canSelect -> Color.Unspecified.copy(alpha = 0.3f)
                            else -> Color.Unspecified
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillMakerAtk(
    wave: Int,
    turn: Int,
    onNextWave: (AutoSkillAction.Atk) -> Unit,
    onNextTurn: (AutoSkillAction.Atk) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            FGATitle(
                stringResource(R.string.skill_maker_atk_header),
                modifier = Modifier.align(Alignment.Center)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(stringResource(R.string.skill_maker_main_wave, wave))

                Text(stringResource(R.string.skill_maker_main_turn, turn))
            }
        }

        var npSequence by rememberSaveable { mutableStateOf("") }
        var cardsBeforeNp by rememberSaveable { mutableIntStateOf(0) }

        var numberOfNPs by rememberSaveable { mutableIntStateOf(0) }

        SelectNps(
            numberOfCardsSelected = cardsBeforeNp,
            npSequence = npSequence,
            onNpSequenceChange = { NPs ->
                npSequence = NPs
                numberOfNPs = (1..3).count { it.toString() in npSequence }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {


            CardsBeforeNp(
                modifier = Modifier.align(Alignment.CenterStart),
                numberOfNpSelected = numberOfNPs,
                cardsBeforeNp = cardsBeforeNp,
                onCardsBeforeNpChange = { cardsBeforeNp = it }
            )


            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
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