package io.github.fate_grand_automata.ui.skill_maker

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.fate_grand_automata.R
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.ui.FgaScreen
import io.github.fate_grand_automata.ui.OnPause
import io.github.fate_grand_automata.ui.PreventRtl
import io.github.fate_grand_automata.ui.dialog.FgaDialog
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice3
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice2
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChoice2Target
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChangeNpType3
import io.github.fate_grand_automata.ui.skill_maker.special.SkillMakerChangeNpType2

@AndroidEntryPoint
class SkillMakerActivity : AppCompatActivity() {
    val vm: SkillMakerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FgaScreen {
                PreventRtl {
                    SkillMakerUI(
                        vm = vm,
                        exit = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillMakerUI(
    vm: SkillMakerViewModel,
    exit: () -> Unit
) {
    OnPause {
        vm.saveState()
    }

    val exitConfirmDialog = FgaDialog()
    exitConfirmDialog.build {
        title(stringResource(R.string.skill_maker_confirm_exit_title))
        message(stringResource(R.string.skill_maker_confirm_exit_message))

        buttons(
            onSubmit = exit
        )
    }

    val clearConfirmDialog = FgaDialog()
    clearConfirmDialog.build {
        title(stringResource(R.string.skill_maker_confirm_clear_title))
        message(stringResource(R.string.skill_maker_confirm_clear_message))

        buttons(
            onSubmit = { vm.clearAll() }
        )
    }

    BackHandler {
        if (vm.navigation.value is SkillMakerNav.Main) {
            exitConfirmDialog.show()
        } else vm.navigation.value = SkillMakerNav.Main
    }

    val (current, navigate) = vm.navigation

    val turn by vm.turn
    val wave by vm.wave

    Crossfade(
        targetState = current,
        animationSpec = spring(),
        label = "Skill Maker Navigation Animation"
    ) { nav ->
        when (nav) {
            SkillMakerNav.Atk -> {
                SkillMakerAtk(
                    wave = wave,
                    turn = turn,
                    onNextWave = { vm.nextWave(it) },
                    onNextTurn = { vm.nextTurn(it) }
                )
            }

            is SkillMakerNav.ChangeNpType2 -> {
                SkillMakerChangeNpType2(
                    onTargetLeft = { vm.targetSkill(ServantTarget.Left) },
                    onTargetRight = { vm.targetSkill(ServantTarget.Right) }
                )
            }

            SkillMakerNav.Main -> {
                SkillMakerMain(
                    vm = vm,
                    onCommandSpell = {
                        navigate(SkillMakerNav.CommandSpell(it))
                    },
                    onMasterSkills = { navigate(SkillMakerNav.MasterSkills) },
                    onAtk = { navigate(SkillMakerNav.Atk) },
                    onSkill = { vm.initSkill(it) },
                    onSkillNoTarget = { vm.noTargetSkill(it) },
                    onClear = { clearConfirmDialog.show() },
                    onDone = {
                        vm.battleConfig.skillCommand = vm.finish()
                        exit()
                    }
                )
            }

            SkillMakerNav.MasterSkills -> {
                SkillMakerMasterSkills(
                    onMasterSkill = { vm.initSkill(it) },
                    onMasterSkillNoTarget = { vm.noTargetSkill(it) },
                    onOrderChange = { navigate(SkillMakerNav.OrderChange) }
                )
            }

            is SkillMakerNav.CommandSpell -> {
                SkillMakerCommandSpells(
                    remaining = nav.cs,
                    onCommandSpell = {
                        vm.initSkill(it)
                    },
                    onBack = {
                        navigate(SkillMakerNav.Main)
                    }
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
                    slot = nav.skill.slot(),
                    onSkillTarget = { vm.targetSkill(it) },
                    onNpType2 = {
                        navigate(SkillMakerNav.ChangeNpType2(nav.skill))
                    },
                    onNpType3 = {
                        navigate(SkillMakerNav.ChangeNpType3(nav.skill))
                    },
                    onChoice2 = { slot ->
                        navigate(SkillMakerNav.Choice2(nav.skill, slot))
                    },
                    onTransform = {
                        vm.targetSkill(ServantTarget.Transform)
                    },
                    onChoice3 = { slot ->
                        navigate(SkillMakerNav.Choice3(nav.skill, slot))
                    }
                )
            }

            is SkillMakerNav.ChangeNpType3 -> {
                SkillMakerChangeNpType3(
                    onSkillTarget = { vm.targetSkill(it) }
                )
            }

            is SkillMakerNav.Choice2 -> {
                SkillMakerChoice2(
                    slot = nav.slot,
                    onOption1 = { vm.targetSkill(ServantTarget.SpecialTarget.Choice2OptionA) },
                    onOption2 = { vm.targetSkill(ServantTarget.SpecialTarget.Choice2OptionB) },
                    goToTarget = nav.skill in Skill.Servant.skill2,
                    onTarget = { firstTarget -> navigate(SkillMakerNav.Choice2Target(nav.skill, firstTarget)) }
                )
            }

            is SkillMakerNav.Choice2Target -> {
                SkillMakerChoice2Target(onSkillTarget = { vm.targetSkill(listOf(nav.firstTarget, it)) })
            }

            is SkillMakerNav.Choice3 -> {
                SkillMakerChoice3(
                    slot = nav.slot,
                    onSkillTarget = { servantTarget ->
                        vm.targetSkill(servantTarget)
                    },
                )
            }
        }
    }
}