package com.mathewsachin.fategrandautomata.ui.skill_maker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import com.mathewsachin.fategrandautomata.R
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.scripts.models.Skill
import com.mathewsachin.fategrandautomata.ui.FgaTheme
import com.mathewsachin.fategrandautomata.ui.PreventRtl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SkillMakerActivity : ComponentActivity() {
    val vm: SkillMakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FgaTheme {
                PreventRtl {
                    SkillMakerUI(
                        vm = vm,
                        onClear = {
                            AlertDialog.Builder(this)
                                .setTitle(R.string.skill_maker_confirm_clear_title)
                                .setMessage(R.string.skill_maker_confirm_clear_message)
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes) { _, _ -> vm.clearAll() }
                                .show()
                        },
                        onDone = {
                            vm.battleConfig.skillCommand = vm.finish()
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (vm.navigation.value is SkillMakerNav.Main) {
            AlertDialog.Builder(this)
                .setMessage(R.string.skill_maker_confirm_exit_message)
                .setTitle(R.string.skill_maker_confirm_exit_title)
                .setPositiveButton(android.R.string.yes) { _, _ -> super.onBackPressed() }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }
        else vm.navigation.value = SkillMakerNav.Main
    }

    override fun onPause() {
        vm.saveState()

        super.onPause()
    }
}

@Composable
fun SkillMakerUI(
    vm: SkillMakerViewModel,
    onClear: () -> Unit,
    onDone: () -> Unit
) {
    val (current, navigate) = vm.navigation

    Crossfade(
        current,
        animationSpec = spring()
    ) { nav ->
        when (nav) {
            SkillMakerNav.Atk -> {
                SkillMakerAtk(
                    onNextWave = { vm.nextStage(it) },
                    onNextTurn = { vm.nextTurn(it) }
                )
            }
            is SkillMakerNav.Emiya -> {
                SkillMakerEmiya(
                    onArts = { vm.targetSkill(ServantTarget.Left) },
                    onBuster = { vm.targetSkill(ServantTarget.Right) }
                )
            }
            SkillMakerNav.Main -> {
                SkillMakerMain(
                    vm = vm,
                    onMasterSkills = { navigate(SkillMakerNav.MasterSkills) },
                    onAtk = { navigate(SkillMakerNav.Atk) },
                    onSkill = { vm.initSkill(it) },
                    onClear = onClear,
                    onDone = onDone
                )
            }
            SkillMakerNav.MasterSkills -> {
                SkillMakerMasterSkills(
                    onMasterSkill = { vm.initSkill(it) },
                    onOrderChange = { navigate(SkillMakerNav.OrderChange) }
                )
            }
            SkillMakerNav.OrderChange -> {
                SkillMakerOrderChange(
                    onCommit = { starting, sub ->
                        vm.commitOrderChange(starting, sub)
                    },
                    onCancel = { vm.back() }
                )
            }
            is SkillMakerNav.SkillTarget -> {
                SkillMakerTarget(
                    onSkillTarget = { vm.targetSkill(it) },
                    showEmiya = nav.skill in listOf(
                        Skill.Servant.A3,
                        Skill.Servant.B3,
                        Skill.Servant.C3
                    ),
                    onEmiya = {
                        navigate(SkillMakerNav.Emiya(nav.skill))
                    },
                    showSpaceIshtar = nav.skill in listOf(
                        Skill.Servant.A2,
                        Skill.Servant.B2,
                        Skill.Servant.C2
                    ),
                    onSpaceIshtar = {
                        navigate(SkillMakerNav.SpaceIshtar(nav.skill))
                    }
                )
            }
            is SkillMakerNav.SpaceIshtar -> {
                SkillMakerSpaceIshtar(
                    onSkillTarget = { vm.targetSkill(it) }
                )
            }
        }
    }
}