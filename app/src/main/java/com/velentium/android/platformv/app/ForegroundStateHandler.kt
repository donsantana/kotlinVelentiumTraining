package com.velentium.android.platformv.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

interface ForegroundStateHandler: LifecycleObserver {
    val isForeground: Boolean

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground()
}