package com.velentium.android.platformv.utils.ui

/**
 * Generic class to allow for single use events with [LiveData]
 */
open class LiveDataEvent<out T>(private val value: T) {
    @Suppress("MemberVisibilityCanBePrivate")
    var isHandled = false
        private set

    val unhandledValue: T?
        get() {
            return if (!isHandled) {
                isHandled = true
                value
            } else {
                null
            }
        }

    val peekValue: T
        get() = value
}