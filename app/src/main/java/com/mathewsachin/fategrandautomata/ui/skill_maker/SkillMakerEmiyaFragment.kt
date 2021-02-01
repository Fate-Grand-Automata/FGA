package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.ui.prefs.compose.ComposePreferencesTheme

@AndroidEntryPoint
class SkillMakerEmiyaFragment : Fragment() {
    val vm: SkillMakerViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                ComposePreferencesTheme {
                    Surface {
                        EmiyaType(
                            onArts = { onSkillTarget(arts) },
                            onBuster = { onSkillTarget(buster) }
                        )
                    }
                }
            }
        }

    private val arts = ServantTarget.Left.autoSkillCode
    private val buster = ServantTarget.Right.autoSkillCode

    private fun onSkillTarget(TargetCommand: Char) {
        vm.targetSkill(TargetCommand)

        findNavController().popBackStack()
    }
}

@Composable
fun EmiyaType(
    onArts: () -> Unit,
    onBuster: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(
            stringResource(R.string.skill_maker_emiya),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Button(
                onClick = onArts,
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorArtsResist)),
                modifier = Modifier.size(120.dp)
            ) {
                Text(stringResource(R.string.skill_maker_arts))
            }

            Button(
                onClick = onBuster,
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.colorBuster)),
                modifier = Modifier.size(120.dp)
            ) {
                Text(stringResource(R.string.skill_maker_buster))
            }
        }
    }
}

@Preview(widthDp = 600, heightDp = 300)
@Composable
fun TestEmiya() {
    EmiyaType(onArts = { }, onBuster = { })
}