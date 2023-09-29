package io.github.fate_grand_automata.ui.card_priority

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.CardScore
import io.github.fate_grand_automata.ui.customFGAColors
import io.github.fate_grand_automata.ui.drag_sort.DragSort

@Composable
fun CardPriorityDragSort(
    scores: MutableList<CardScore>,
    onSubmit: (MutableList<CardScore>) -> Unit
) {
    var cloneScores by remember { mutableStateOf(scores.toList()) }

    DragSort(
        titleText = stringResource(R.string.card_priority),
        messageText = stringResource(R.string.p_battle_config_instructions_drag_and_drop),
        items = cloneScores,
        itemContent = { item ->
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = stringResource(R.string.card_priority)
                    )
                },
                trailingContent = {
                    // Added Blank icon to make the Text Centered
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        tint = Color.Transparent
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = getCardScoreColor(item = item)
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                headlineContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = getCardScoreName(item),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            )
        },
        initialContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                cloneScores.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = getCardScoreColor(item = item)
                        ),
                        shape = RectangleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.toString(),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                    }
                }

            }
        },
        onSubmit = {
            cloneScores = it
            onSubmit(cloneScores.toMutableList())
        }
    )
}

@Composable
fun getCardScoreColor(item: CardScore) = when (item.type) {
    CardTypeEnum.Buster -> when (item.affinity) {
        CardAffinityEnum.Normal -> MaterialTheme.customFGAColors.colorBuster
        CardAffinityEnum.Weak -> MaterialTheme.customFGAColors.colorBusterWeak
        CardAffinityEnum.Resist -> MaterialTheme.customFGAColors.colorBusterResist
    }

    CardTypeEnum.Arts -> when (item.affinity) {
        CardAffinityEnum.Normal -> MaterialTheme.customFGAColors.colorArts
        CardAffinityEnum.Weak -> MaterialTheme.customFGAColors.colorArtsWeak
        CardAffinityEnum.Resist -> MaterialTheme.customFGAColors.colorArtsResist
    }

    CardTypeEnum.Quick -> when (item.affinity) {
        CardAffinityEnum.Normal -> MaterialTheme.customFGAColors.colorQuick
        CardAffinityEnum.Weak -> MaterialTheme.customFGAColors.colorQuickWeak
        CardAffinityEnum.Resist -> MaterialTheme.customFGAColors.colorQuickResist
    }

    CardTypeEnum.Unknown -> Color("#1B3147".toColorInt())
}

@Composable
private fun getCardScoreName(item: CardScore) =
    buildAnnotatedString {
        when (item.affinity) {
            CardAffinityEnum.Normal -> {
                pushStyle(style = SpanStyle(fontWeight = FontWeight.Medium, letterSpacing = TextUnit(0.5f, TextUnitType.Sp)))
            }

            CardAffinityEnum.Weak -> {
                pushStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        letterSpacing = TextUnit(1f, TextUnitType.Sp)
                    )
                )
                append("${item.affinity} ".uppercase())
            }

            CardAffinityEnum.Resist -> {
                pushStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic))
                append("${item.affinity} ".uppercase())
            }
        }

        append("${item.type}".uppercase())
        toAnnotatedString()
    }