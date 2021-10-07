package com.velentium.android.platformv.ble.core.common

import android.bluetooth.BluetoothDevice

/**
 * Enum-type Sealed class to indicate [BleConnectionStatus]
 */
sealed class BleConnectionStatus {
    data class CONNECTED(val device: BluetoothDevice): BleConnectionStatus() {
        override fun equals(other: Any?): Boolean {
            if (other is CONNECTED) {
                return other.device.address == this.device.address
            }
            return false
        }

        override fun hashCode(): Int {
            return device.hashCode()
        }
    }

    data class DISCONNECTED(val isExpected: Boolean = true) : BleConnectionStatus() {
        override fun equals(other: Any?): Boolean {
            return (other is DISCONNECTED)
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }
}
