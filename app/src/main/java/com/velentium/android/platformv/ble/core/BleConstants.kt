package com.velentium.android.platformv.ble.core

import android.os.ParcelUuid
import java.util.*

object BleConstants {
    const val CONNECTION_TIMEOUT: Long = 10L * 1000L
    const val SCAN_TIMEOUT: Long = 20L * 1000L
    const val DEFAULT_MTU_SIZE = 260
    const val DEFAULT_OPERATION_TIMEOUT: Long = 3500L

    val UART_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    val UART_RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    val UART_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
}

val UUID.parcelUuid: ParcelUuid
    get() = ParcelUuid(this)