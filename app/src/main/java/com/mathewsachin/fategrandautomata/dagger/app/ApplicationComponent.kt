package com.mathewsachin.fategrandautomata.dagger.app

import com.mathewsachin.fategrandautomata.dagger.service.ScriptRunnerServiceComponent
import com.mathewsachin.fategrandautomata.ui.MainActivity
import com.mathewsachin.fategrandautomata.ui.auto_skill_list.AutoSkillListFragment
import com.mathewsachin.fategrandautomata.ui.auto_skill_maker.AutoSkillMakerActivity
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityFragment
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemSettingsFragment
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsFragment
import com.mathewsachin.fategrandautomata.ui.prefs.MoreSettingsFragment
import com.mathewsachin.fategrandautomata.ui.prefs.RefillSettingsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, AppContextModule::class])
interface ApplicationComponent {
    fun scriptRunnerServiceComponent(): ScriptRunnerServiceComponent.Builder

    fun inject(into: MainActivity)
    fun inject(into: AutoSkillListFragment)
    fun inject(into: CardPriorityFragment)
    fun inject(into: AutoSkillItemSettingsFragment)
    fun inject(into: MainSettingsFragment)
    fun inject(into: MoreSettingsFragment)
    fun inject(into: RefillSettingsFragment)
    fun inject(into: AutoSkillMakerActivity)
}