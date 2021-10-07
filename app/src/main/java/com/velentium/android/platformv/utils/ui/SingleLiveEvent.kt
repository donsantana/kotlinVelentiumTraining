package com.velentium.android.platformv.utils.ui

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Utility class for using as a replacement for [MutableLiveData],
 * when you don't want it to notify more than once.  Standard [MutableLiveData],
 * will notify again upon configuration changes.
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val isPending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(
                TAG,
                "Multiple observers registered, but only one will be notified of changes."
            )
        }
        super.observe(owner, { t ->
            if (isPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(value: T?) {
        isPending.set(true)
        super.setValue(value)
    }

    /**
     * Useful for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

    companion object {
        val TAG: String = SingleLiveEvent::class.java.simpleName
    }
}