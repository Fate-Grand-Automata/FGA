package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportFriendChecker @Inject constructor(
    api: IFgoAutomataApi
): IFgoAutomataApi by api {
    fun isFriend(bounds: SupportBounds? = null): Boolean {
        val friendRegion = bounds?.region?.intersect(locations.support.friendRegion)
            ?: locations.support.friendRegion

        return sequenceOf(
            images[Images.Friend],
            images[Images.Follow]
        ).any { it in friendRegion }
    }
}