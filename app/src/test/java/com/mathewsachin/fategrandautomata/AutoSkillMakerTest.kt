package com.mathewsachin.fategrandautomata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillMakerViewModel
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillMakerViewState
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class AutoSkillMakerTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    fun getViewModel() = AutoSkillMakerViewModel(SavedStateHandle())

    @Test
    fun test_empty() {
        val vm = getViewModel()

        assert(vm.finish().isEmpty())
    }

    fun test_skill(block: (AutoSkillMakerViewModel, Char) -> Unit) {
        for (c in 'a'..'l') {
            val vm = getViewModel()

            vm.onSkill(c)
            block(vm, c)
        }
    }

    @Test
    fun test_skill() {
        for (c in 'a'..'l') {
            val vm = getViewModel()

            vm.onSkill(c)
            vm.onSkillTarget()

            Assert.assertEquals(vm.finish(), "$c")
        }
    }

    @Test
    fun test_skill_non_targeted() {
        test_skill { vm, c ->
            vm.onSkillTarget()

            Assert.assertEquals(vm.finish(), "$c")
        }
    }

    @Test
    fun test_skill_targeted() {
        for (target in '1'..'3') {
            test_skill { vm, c ->
                vm.onSkillTarget(target)

                Assert.assertEquals(vm.finish(), "${c}${target}")
            }
        }
    }

    @Test
    fun test_np() {
        for (np in '4'..'6') {
            val vm = getViewModel()

            vm.onNpClick(np)

            Assert.assertEquals(vm.finish(), "$np")
        }
    }

    @Test
    fun test_go_back() {
        val vm = getViewModel()

        vm.goToAtk()
        vm.goBack()

        Assert.assertEquals(vm.currentView.value, AutoSkillMakerViewState.Main)
    }
}