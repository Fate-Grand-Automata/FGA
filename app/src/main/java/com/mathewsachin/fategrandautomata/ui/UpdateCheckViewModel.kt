package com.mathewsachin.fategrandautomata.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathewsachin.fategrandautomata.util.UpdateCheckResult
import com.mathewsachin.fategrandautomata.util.UpdateChecker
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

class UpdateCheckViewModel @ViewModelInject constructor(
    val updateChecker: UpdateChecker
) : ViewModel() {
    private val updateCheckResult = CompletableDeferred<UpdateCheckResult>()

    init {
        viewModelScope.launch {
            updateCheckResult.complete(updateChecker.check())
        }
    }

    suspend fun check() = updateCheckResult.await()
}