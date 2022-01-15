package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.libautomata.dagger.ScriptScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ScriptScope
class FriendSelection @Inject constructor(
    api: IFgoAutomataApi
): IFgoAutomataApi by api {
    suspend fun check(friendNames: List<String>, bounds: SupportBounds): Boolean {
        if (friendNames.isEmpty())
            return true

        val searchRegion = bounds.region intersect locations.support.friendRegion ?: return false

        return coroutineScope {
            friendNames
                .flatMap { entry -> images.loadSupportPattern(SupportImageKind.Friend, entry) }
                .map {
                    async { it in searchRegion }
                }
                .any { it.await() }
        }
    }
}

