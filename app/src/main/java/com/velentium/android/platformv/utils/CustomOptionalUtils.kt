package com.velentium.android.platformv.utils

import java.util.*

/**
 * Inline function that converts an [Optional]<[T]> to [T]?
 */
inline fun <reified T> Optional<T>.getOptional(): T? {
    return this.takeIf { isPresent }?.get()
}