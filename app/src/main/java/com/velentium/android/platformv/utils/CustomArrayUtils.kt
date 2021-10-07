package com.velentium.android.platformv.utils

//region For Each Group Functions
// Useful when trying to operate on "sub groups" contained in the [Array]

inline fun <reified T> ByteArray.forEachGroup(
    size: Int,
    cutOffRemainder: Boolean = false,
    block: (ByteArray) -> T
): Array<T> {
    var arraySize = this.size / size
    if (!cutOffRemainder && this.size % size != 0) {
        arraySize += 1
    }
    return Array(size = arraySize) {
        val startIndex: Int = it * size
        val endIndex: Int = kotlin.math.min(startIndex + size, this.size)
        val group = this.copyOfRange(fromIndex = startIndex, toIndex = endIndex)
        block.invoke(group)
    }
}

inline fun <reified T> ByteArray.forEachGroupIndexed(
    size: Int,
    cutOffRemainder: Boolean = false,
    block: (Int, ByteArray) -> T
): Array<T> {
    var arraySize = this.size / size
    if (!cutOffRemainder && this.size % size != 0) {
        arraySize += 1
    }
    return Array(size = arraySize) {
        val startIndex: Int = it * size
        val endIndex: Int = kotlin.math.min(startIndex + size, this.size)
        val group = this.copyOfRange(fromIndex = startIndex, toIndex = endIndex)
        block.invoke(it, group)
    }
}

inline fun <T, reified R> Array<T>.forEachGroup(
    size: Int,
    cutOffRemainder: Boolean = false,
    block: (Array<T>) -> R
): Array<R> {
    var arraySize = this.size / size
    if (!cutOffRemainder && this.size % size != 0) {
        arraySize += 1
    }
    return Array(size = arraySize) {
        val startIndex: Int = it * size
        val endIndex: Int = kotlin.math.min(startIndex + size, this.size)
        val group = this.copyOfRange(fromIndex = startIndex, toIndex = endIndex)
        block.invoke(group)
    }
}

inline fun <T, reified R> Array<T>.forEachGroupIndexed(
    size: Int,
    cutOffRemainder: Boolean = false,
    block: (Int, Array<T>) -> R
): Array<R> {
    var arraySize = this.size / size
    if (!cutOffRemainder && this.size % size != 0) {
        arraySize += 1
    }
    return Array(size = arraySize) {
        val startIndex: Int = it * size
        val endIndex: Int = kotlin.math.min(startIndex + size, this.size)
        val group = this.copyOfRange(fromIndex = startIndex, toIndex = endIndex)
        block.invoke(it, group)
    }
}
//endregion

//region Custom Copy Extensions

fun <T> Array<T>.copyOfRange(fromIndex: Int, size: Int): Array<T> {
    return this.copyOfRange(fromIndex = fromIndex, toIndex = fromIndex + size)
}

/* Custom Arithmetic Extensions */
fun DoubleArray.averageAboveZero(): Double {
    return this.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
}

//endregion

//region Map functions

/**
 * Convenient map function that maps from array to array
 */
inline fun <T, reified R> Array<T>.mapToArray(block: (T) -> R): Array<R> {
    return this.map { block(it) }
        .toTypedArray()
}

//endregion