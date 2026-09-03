package io.github.fate_grand_automata.ui.pref_support

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.fate_grand_automata.prefs.core.PrefsCore

@HiltViewModel(assistedFactory = PreferredSupportViewModel.Factory::class)
class PreferredSupportViewModel @AssistedInject constructor(
    prefsCore: PrefsCore,
    @Assisted id: String
): ViewModel() {
    val supportPrefs = prefsCore.forBattleConfig(id).support

    @AssistedFactory
    interface Factory {
        fun create(id: String): PreferredSupportViewModel
    }
}