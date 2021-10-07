package com.velentium.android.platformv.ble.core.common

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.velentium.android.platformv.ble.core.BleConstants
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.data.Data
import java.nio.charset.Charset


@Suppress("MemberVisibilityCanBePrivate")
interface BaseBleManager {
    companion object {
        // bug with android gatt client, gatt client should be destroyed/recreated
        const val GATT_CORRUPTED: Int = 133

        const val UNDEFINED_STATUS: Int = -2000

        @JvmStatic
        fun statusString(status: Int): String {
            return when(status) {
                FailCallback.REASON_DEVICE_DISCONNECTED -> "REASON_DEVICE_DISCONNECTED($status)"                // -1
                FailCallback.REASON_DEVICE_NOT_SUPPORTED -> "REASON_DEVICE_NOT_SUPPORTED($status)"              // -2
                FailCallback.REASON_NULL_ATTRIBUTE -> "REASON_NULL_ATTRIBUTE($status)"                          // -3
                FailCallback.REASON_REQUEST_FAILED -> "REASON_REQUEST_FAILED($status)"                          // -4
                FailCallback.REASON_TIMEOUT -> "REASON_TIMEOUT($status)"                                        // -5
                FailCallback.REASON_VALIDATION -> "REASON_VALIDATION($status)"                                  // -6
                FailCallback.REASON_CANCELLED -> "REASON_CANCELLED($status)"                                    // -7
                FailCallback.REASON_BLUETOOTH_DISABLED -> "REASON_BLUETOOTH_DISABLED($status)"                  // -100
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> "GATT_CONNECTION_CONGESTED($status)"                 // 143
                BluetoothGatt.GATT_FAILURE -> "GATT_FAILURE($status)"                                           // 257
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "GATT_INSUFFICIENT_AUTHENTICATION($status)"   // 5
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "GATT_INSUFFICIENT_ENCRYPTION($status)"           // 15
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE_LENGTH($status)"         // 13
                BluetoothGatt.GATT_INVALID_OFFSET -> "GATT_INVALID_OFFSET($status)"                             // 7
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> "GATT_READ_NOT_PERMITTED($status)"                     // 2
                BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "GATT_REQUEST_NOT_SUPPORTED($status)"               // 6
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "GATT_WRITE_NOT_PERMITTED($status)"                   // 3
                GATT_CORRUPTED -> "GATT_CORRUPTED($status)"                                                         // 133
                UNDEFINED_STATUS -> "UNDEFINED_STATUS($status)"                                                 // -2000
                else -> "UNKNOWN_STATUS($status)"
            }
        }
    }

    /**
     * The [BaseBleScanner]
     */
    val bleScanner: BaseBleScanner

    /**
     * Publishes [BleAdapterState]
     */
    val adapterStateObservable: Observable<BleAdapterState>

    /**
     * Contains the negotiated MTU size
     */
    val mtuSize: Int

    /**
     * true if Bluetooth is not disabled on the device, false otherwise.
     */
    val isBluetoothEnabled: Boolean

    /**
     * Indicates if a device is currently connected, or not.
     */
    val isDeviceConnected: Boolean

    /**
     * Indicates if a device is currently bonded, or not.
     */
    val isDeviceBonded: Boolean

    /**
     * The mac address of the currently connected device, or null.
     */
    val currentDeviceMacAddress: String?

    /**
     * The currently connected device, or null.
     */
    val currentDevice: BluetoothDevice?

    /**
     * Contains a list of [BluetoothGattCharacteristic] that are currently
     * setup/enabled for notifications.
     */
    val activeNotificationListeners: Set<BluetoothGattCharacteristic>

    /**
     * Indicates device connection status of either [BleConnectionStatus.CONNECTED],
     * or [BleConnectionStatus.DISCONNECTED]
     */
    val connectionStatusFlowable: Flowable<BleConnectionStatus>

    /**
     * Registers for BLE adapter changes
     */
    fun registerAdapterStateReceiver(context: Context)

    /**
     * Unregisters for BLE adapter changes
     */
    fun unregisterAdapterStateReceiver(context: Context, close: Boolean = true)

    /**
     * Automatically enable Bluetooth
     * @return true if enabled, false if it was already enabled
     */
    fun enableBluetooth(): Boolean

    /**
     * Automatically disable Bluetooth
     * @return true if disabled, false if it was already disabled
     */
    fun disableBluetooth(): Boolean

    /**
     * Restart the Bluetooth adapter
     * @return [Single] emitting so it can be chained.
     */
    fun restartBluetooth(): Single<Unit>

    /**
     * Prompts the user to enable Bluetooth, if it's disabled.
     */
    fun promptToEnableBluetooth(activity: Activity)

    /**
     * Attempts to request a MTU size of [requestedSize]
     */
    fun negotiateMtuSize(requestedSize: Int = BleConstants.DEFAULT_MTU_SIZE)

    /**
     * Connect to the given device.
     * @return [Single] emitting the connected [BluetoothDevice].
     */
    fun connectDevice(device: BluetoothDevice): Single<BluetoothDevice>

    /**
     * Disconnect from the [currentDevice]
     */
    fun disconnectDevice(): Single<Unit>

    /**
     * Sets up notifications for the given [identifier].
     */
    fun setUpNotificationListener(
        identifier: BluetoothGattCharacteristic,
        onNotified: (BluetoothGattCharacteristic, Data) -> Unit,
        onEnabled: ((Boolean, BleManagerError?) -> Unit)? = null
    )

    /**
     * Reads from the given [BluetoothGattCharacteristic]
     */
    fun read(identifier: BluetoothGattCharacteristic): Single<Data>

    /**
     * Writes to the given [BluetoothGattCharacteristic]
     */
    fun write(identifier: BluetoothGattCharacteristic, data: Data): Single<Unit>

    /**
     * Writes to the given [BluetoothGattCharacteristic]
     */
    fun write(identifier: BluetoothGattCharacteristic, bytes: ByteArray): Single<Unit>

    /**
     * Writes to the given [BluetoothGattCharacteristic]
     */
    fun write(
        identifier: BluetoothGattCharacteristic,
        value: String,
        charset: Charset = Charset.forName("UTF-8")
    ): Single<Unit>

    /**
     * Stops notifications for the given [identifier].
     */
    fun stopListener(identifier: BluetoothGattCharacteristic)

    /**
     * Stops all notifications for any active listeners.
     */
    fun stopAllListeners()
}