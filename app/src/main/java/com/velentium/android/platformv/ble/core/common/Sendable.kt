package com.velentium.android.platformv.ble.core.common

import no.nordicsemi.android.ble.data.Data

//region Sendable

interface Sendable {
    fun toBluetoothData(): Data
}

//endregion

//region Receivable

sealed class ReceivableError(message: String) : IllegalArgumentException(message) {
    class ParseFailure(message: String) : ReceivableError(message)
}

abstract class Receivable {
    abstract val dataStructureSize: Int
    abstract val payload: ByteArray

    @Throws(ReceivableError.ParseFailure::class)
    constructor(bluetoothData: Data)

    @Throws(ReceivableError.ParseFailure::class)
    constructor(bluetoothData: Data, fromIndex: Int, toIndex: Int? = null)
}

@Suppress("MemberVisibilityCanBePrivate")
abstract class ValidatedReceivable @Throws(ReceivableError.ParseFailure::class) constructor(
    bluetoothData: Data,
    expectedSize: Int
) : Receivable(bluetoothData) {

    final override val dataStructureSize: Int = expectedSize
    final override val payload: ByteArray = bluetoothData.value ?: byteArrayOf()

    val isValidPayload: Boolean
        get() = payload.count() >= dataStructureSize

    @Throws(ReceivableError.ParseFailure::class)
    constructor(bluetoothData: Data) : this(bluetoothData, 1)

    @Throws(ReceivableError.ParseFailure::class)
    constructor(
        bluetoothData: Data,
        fromIndex: Int,
        toIndex: Int?
    ) : this(bluetoothData = bluetoothData.value
        ?.takeIf { it.count() > fromIndex }
        ?.let {
            val endIndex = toIndex ?: it.count()
            val subData = it.copyOfRange(fromIndex, endIndex)
            return@let subData.toData()
        } ?: throw ReceivableError.ParseFailure("Invalid data: $bluetoothData")
    )

    init {
        if (!isValidPayload) {
            throw ReceivableError.ParseFailure("Invalid size, expected: $dataStructureSize")
        }
    }
}

//endregion

//region Extension Functions

fun ByteArray.toData(): Data = Data(this)

@ExperimentalUnsignedTypes
fun UByteArray.toData(): Data = Data(this.map { it.toByte() }.toByteArray())

//endregion