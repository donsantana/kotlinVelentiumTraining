@file:Suppress("MemberVisibilityCanBePrivate")

package com.velentium.android.platformv.ble.core.common


sealed class BleManagerError(message: String) : Exception(message) {
    open class BleManagerStatusError(
        message: String,
        val status: Int = BaseBleManager.UNDEFINED_STATUS
    ) : Exception(message) {
        override fun toString(): String {
            return "Message: ${message ?: ""}, Status: ${BaseBleManager.statusString(status)}"
        }
    }

    class DeviceNotConnectedError(message: String = "Device is not connected!") :
        BleManagerError(message)

    class DeviceConnectionError(message: String, status: Int = BaseBleManager.UNDEFINED_STATUS) :
        BleManagerStatusError(message, status)

    class DeviceDisconnectionError(message: String, status: Int = BaseBleManager.UNDEFINED_STATUS) :
        BleManagerStatusError(message, status)

    class BleScannerError(val errorCode: Int) :
        BleManagerError("Scanning failed with error code: $errorCode")

    class BlePermissionsError(message: String) : BleManagerError(message)

    class GattEnableNotificationError(message: String) : BleManagerError(message)
    class GattReadError(message: String, status: Int = BaseBleManager.UNDEFINED_STATUS) :
        BleManagerStatusError(message, status)

    class GattWriteError(message: String, status: Int = BaseBleManager.UNDEFINED_STATUS) :
        BleManagerStatusError(message, status)
}