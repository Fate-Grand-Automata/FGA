package com.mathewsachin.fategrandautomata.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import java.util.concurrent.CountDownLatch

class GestureCompletedCallback : AccessibilityService.GestureResultCallback() {
    private val latch = CountDownLatch(1)

    override fun onCompleted(gestureDescription: GestureDescription?) {
        latch.countDown()
        super.onCompleted(gestureDescription)
    }

    override fun onCancelled(gestureDescription: GestureDescription?) {
        latch.countDown()
        super.onCancelled(gestureDescription)
    }

    fun waitTillFinish() {
        latch.await()
    }
}