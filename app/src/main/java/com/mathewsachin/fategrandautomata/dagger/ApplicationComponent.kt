package com.mathewsachin.fategrandautomata.dagger

import com.mathewsachin.fategrandautomata.ui.AutoSkillItemActivity
import com.mathewsachin.fategrandautomata.ui.AutoSkillListActivity
import com.mathewsachin.fategrandautomata.ui.MainActivity
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityActivity
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemSettingsFragment
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsFragment
import com.mathewsachin.fategrandautomata.ui.prefs.MoreSettingsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, AppContextModule::class])
interface ApplicationComponent {
    fun scriptRunnerServiceComponent(): ScriptRunnerServiceComponent.Builder

    fun inject(into: MainActivity)
    fun inject(into: AutoSkillItemActivity)
    fun inject(into: AutoSkillListActivity)
    fun inject(into: CardPriorityActivity)
    fun inject(into: AutoSkillItemSettingsFragment)
    fun inject(into: MainSettingsFragment)
    fun inject(into: MoreSettingsFragment)
}