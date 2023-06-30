package io.github.fate_grand_automata.ui.runner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class ScriptRunnerUIStateHolder @Inject constructor() {
    var uiState by mutableStateOf<ScriptRunnerUIState>(ScriptRunnerUIState.Idle)
    var isRecording by mutableStateOf(false)
    var isPlayButtonEnabled by mutableStateOf(true)
}