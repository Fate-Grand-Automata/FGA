package io.github.fate_grand_automata.ui.spam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fate_grand_automata.R

@Composable
fun SkillPriorityItem(
    servantIndex: Int,
    skillIndex: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(dimensionResource(id = R.dimen.skill_box_width))
            .height(40.dp)
            .padding(0.5.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(getServantColor(servantIndex, ColorType.BG)))
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Row 1: Team-slot 1..6
        Text(
            text = servantIndex.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(getServantColor(servantIndex, ColorType.PRIMARY)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Row 2: Skill-slot L/M/R
        Text(
            text = skillIndex.getSkillSlotText(),
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = colorResource(getServantColor(servantIndex, ColorType.SECONDARY)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun Int.getSkillSlotText() = when (this) {
    0 -> "L"
    1 -> "M"
    else -> "R"
}

private fun getServantColor(
    itemIndex: Int,
    type: ColorType
): Int {
    val colorRes = when (itemIndex) {
        1 -> when(type) {
            ColorType.PRIMARY -> R.color.servant1_primary_text
            ColorType.SECONDARY -> R.color.servant1_secondary_text
            ColorType.BG -> R.color.servant1_bg
        }
        2 -> when(type) {
            ColorType.PRIMARY -> R.color.servant2_primary_text
            ColorType.SECONDARY -> R.color.servant2_secondary_text
            ColorType.BG -> R.color.servant2_bg
        }
        3 -> when(type) {
            ColorType.PRIMARY -> R.color.servant3_primary_text
            ColorType.SECONDARY -> R.color.servant3_secondary_text
            ColorType.BG -> R.color.servant3_bg
        }
        4 -> when(type) {
            ColorType.PRIMARY -> R.color.servant4_primary_text
            ColorType.SECONDARY -> R.color.servant4_secondary_text
            ColorType.BG -> R.color.servant4_bg
        }
        5 -> when(type) {
            ColorType.PRIMARY -> R.color.servant5_primary_text
            ColorType.SECONDARY -> R.color.servant5_secondary_text
            ColorType.BG -> R.color.servant5_bg
        }
        else -> when(type) {
            ColorType.PRIMARY -> R.color.servant6_primary_text
            ColorType.SECONDARY -> R.color.servant6_secondary_text
            ColorType.BG -> R.color.servant6_bg
        }
    }
    return colorRes
}

private enum class ColorType { PRIMARY, SECONDARY, BG }

@Preview(name = "Light Mode", widthDp = 40, heightDp = 40)
@Composable
private fun SkillPriorityItemPreview() {
    SkillPriorityItem(servantIndex = 1, skillIndex = 1)
}