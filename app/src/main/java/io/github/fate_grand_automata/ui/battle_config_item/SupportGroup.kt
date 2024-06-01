package io.github.fate_grand_automata.ui.battle_config_item

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.prefs.core.SupportPrefsCore
import io.github.fate_grand_automata.scripts.enums.SupportClass
import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum
import io.github.fate_grand_automata.scripts.enums.canAlsoCheckAll
import io.github.fate_grand_automata.ui.DimmedIcon
import io.github.fate_grand_automata.ui.icon
import io.github.fate_grand_automata.ui.padding
import io.github.fate_grand_automata.ui.prefs.ListPreference
import io.github.fate_grand_automata.ui.prefs.Preference
import io.github.fate_grand_automata.ui.prefs.PreferenceGroupHeader
import io.github.fate_grand_automata.ui.prefs.SingleSelectChipPreference
import io.github.fate_grand_automata.ui.prefs.SwitchPreference
import io.github.fate_grand_automata.ui.prefs.remember
import io.github.fate_grand_automata.util.stringRes
import java.io.File

@Composable
fun SupportGroup(
    config: SupportPrefsCore,
    maxSkillText: String,
    maxAppendText: String,
    goToPreferred: () -> Unit
) {
    val supportMode by config.selectionMode.remember()

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            PreferenceGroupHeader(
                title = stringResource(R.string.p_battle_config_support)
            )

            var supportClass by config.supportClass.remember()

            SupportClassPicker(
                selected = supportClass,
                onSelectedChange = { supportClass = it }
            )

            val canAlsoCheckAll = supportClass.canAlsoCheckAll && supportMode != SupportSelectionModeEnum.Manual

            AnimatedVisibility(canAlsoCheckAll) {
                config.alsoCheckAll.SwitchPreference(
                    title = stringResource(R.string.p_battle_config_support_also_check_all)
                )
            }

            val preferredMode = supportMode == SupportSelectionModeEnum.Preferred

            Row {
                config.selectionMode.ListPreference(
                    title = stringResource(R.string.p_battle_config_support_selection_mode),
                    entries = SupportSelectionModeEnum.entries
                        .associateWith { stringResource(it.stringRes) },
                    modifier = Modifier.weight(1f)
                )

                if (preferredMode) {
                    config.fallbackTo.SingleSelectChipPreference(
                        title = stringResource(R.string.p_battle_config_support_fallback_selection_mode),
                        entries = listOf(
                            SupportSelectionModeEnum.First,
                            SupportSelectionModeEnum.Manual
                        ).associateWith { stringResource(it.stringRes) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(preferredMode) {
                Column {
                    val servants by config.preferredServants.remember()
                    val checkAppend by config.checkAppend.remember()
                    val ces by config.preferredCEs.remember()
                    val cesFormatted by remember {
                        derivedStateOf {
                            ces
                                .map { File(it).nameWithoutExtension }
                                .toSet()
                        }
                    }
                    val friendNames by config.friendNames.remember()
                    val friendNamesFormatted by remember {
                        derivedStateOf {
                            friendNames
                                .map { File(it).nameWithoutExtension }
                                .toSet()
                        }
                    }

                    Preference(
                        title = { Text(stringResource(R.string.p_support_mode_preferred)) },
                        summary = {
                            PreferredSummary(
                                config = config,
                                maxSkillText = maxSkillText,
                                checkAppend = checkAppend,
                                maxAppendText = maxAppendText,
                                servants = servants,
                                ces = cesFormatted,
                                friendNames = friendNamesFormatted
                            )
                        },
                        onClick = goToPreferred
                    )

                    AnimatedVisibility(servants.isEmpty() && ces.isEmpty()) {
                        PreferenceError(
                            stringResource(R.string.support_selection_preferred_not_set)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportClassPicker(
    selected: SupportClass,
    onSelectedChange: (SupportClass) -> Unit
) {
    Card(
        elevation = cardElevation(2.dp),
        modifier = Modifier
            .padding(16.dp, 5.dp)
            .fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items(SupportClass.entries.drop(1)) {
                val isSelected = selected == it
                val transition = updateTransition(isSelected, label = "Selected")
                val alpha by transition.animateFloat(label = "alpha") { selected ->
                    if (selected) 1f else 0.4f
                }
                val borderClass by transition.animateColor(label = "border color") { selected ->
                    if (selected)
                        MaterialTheme.colorScheme.secondary
                    else Color.Transparent
                }

                Image(
                    painterResource(it.drawable),
                    contentDescription = it.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(25.dp)
                        .alpha(alpha)
                        .border(2.dp, borderClass, DiamondShape)
                        .clip(DiamondShape)
                        .clickable {
                            onSelectedChange(
                                if (isSelected)
                                    SupportClass.None
                                else it
                            )
                        }
                )
            }
        }
    }
}

val SupportClass.drawable
    @DrawableRes get() = when (this) {
        SupportClass.None -> R.drawable.ic_dots_horizontal
        SupportClass.All -> R.drawable.support_all
        SupportClass.Saber -> R.drawable.support_saber
        SupportClass.Archer -> R.drawable.support_archer
        SupportClass.Lancer -> R.drawable.support_lancer
        SupportClass.Rider -> R.drawable.support_rider
        SupportClass.Caster -> R.drawable.support_caster
        SupportClass.Assassin -> R.drawable.support_assassin
        SupportClass.Berserker -> R.drawable.support_berserker
        SupportClass.Extra -> R.drawable.support_extra
        SupportClass.Mix -> R.drawable.support_mix
    }

val DiamondShape = CutCornerShape(50)

@Composable
fun PreferredSummary(
    config: SupportPrefsCore,
    maxSkillText: String,
    checkAppend: Boolean,
    maxAppendText: String,
    servants: Set<String>,
    ces: Set<String>,
    friendNames: Set<String>
) {
    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            DimmedIcon(
                icon(R.drawable.ic_crown),
                contentDescription = "crown"
            )

            val text = if (servants.isNotEmpty())
                servants.joinToString()
            else stringResource(R.string.battle_config_support_any)

            Text(
                text,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            if (servants.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SkillDisplay(
                        title = stringResource(id = R.string.p_support_skills),
                        maxText = maxSkillText
                    )
                    if (checkAppend) {
                        Spacer(modifier = Modifier.height(MaterialTheme.padding.extraSmall))
                        SkillDisplay(
                            title = stringResource(id = R.string.p_support_appends),
                            maxText = maxAppendText
                        )
                    }

                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            DimmedIcon(
                icon(R.drawable.ic_card),
                contentDescription = "card"
            )

            val text = if (ces.isNotEmpty())
                ces.joinToString()
            else stringResource(R.string.battle_config_support_any)

            Text(
                text,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )

            if (ces.isNotEmpty()) {
                val mlb by config.mlb.remember()

                if (mlb) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "MLB",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        val friendsOnly by config.friendsOnly.remember()

        if (friendsOnly) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 2.dp)
            ) {
                DimmedIcon(
                    icon(R.drawable.ic_friend),
                    contentDescription = "friend"
                )

                val text = if (friendNames.isNotEmpty())
                    friendNames.joinToString()
                else stringResource(R.string.battle_config_support_any)

                Text(
                    text,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SkillDisplay(
    title: String,
    maxText: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontWeight = FontWeight.Bold
        )
        Card(
            elevation = cardElevation(2.dp)
        ) {
            Text(
                text = maxText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(5.dp, 1.dp)
            )
        }
    }
}