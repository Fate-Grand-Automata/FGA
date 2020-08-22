package com.mathewsachin.fategrandautomata.dagger.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mathewsachin.fategrandautomata.ui.auto_skill_list.AutoSkillListViewModel
import com.mathewsachin.fategrandautomata.ui.card_priority.CardPriorityViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.AutoSkillItemViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.FineTuneSettingsViewModel
import com.mathewsachin.fategrandautomata.ui.prefs.MainSettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(AutoSkillListViewModel::class)
    abstract fun bindAutoSkillListViewModel(viewModel: AutoSkillListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AutoSkillItemViewModel::class)
    abstract fun bindAutoSkillItemViewModel(viewModel: AutoSkillItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainSettingsViewModel::class)
    abstract fun bindMainSettingsViewModel(viewModel: MainSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FineTuneSettingsViewModel::class)
    abstract fun bindFineTuneSettingsViewModel(viewModel: FineTuneSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardPriorityViewModel::class)
    abstract fun bindCardPriorityViewModel(viewModel: CardPriorityViewModel): ViewModel
}