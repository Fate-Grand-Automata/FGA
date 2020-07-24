package com.mathewsachin.fategrandautomata.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewsachin.fategrandautomata.util.UpdateCheckResult
import com.mathewsachin.fategrandautomata.util.UpdateChecker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class UpdateCheckViewModel : ViewModel() {
    private val updateCheckResult = CompletableDeferred<UpdateCheckResult>()
    val updateChecker = UpdateChecker()

    init {
        viewModelScope.launch {
            updateCheckResult.complete(updateChecker.check())
        }
    }

    suspend fun check() = updateCheckResult.await()
}