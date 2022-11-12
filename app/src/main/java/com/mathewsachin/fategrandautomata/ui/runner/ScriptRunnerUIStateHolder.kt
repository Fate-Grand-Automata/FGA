package com.mathewsachin.fategrandautomata.ui.runner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class ScriptRunnerUIStateHolder @Inject constructor() {
    var uiState by mutableStateOf<ScriptRunnerUIState>(ScriptRunnerUIState.Idle)
    var isPlayButtonEnabled by mutableStateOf(true)
}