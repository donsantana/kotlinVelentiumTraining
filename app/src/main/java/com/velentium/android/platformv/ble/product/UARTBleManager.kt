@file:Suppress("ThrowableNotThrown")

package com.velentium.android.platformv.ble.product

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.velentium.android.platformv.ble.core.BleConstants
import com.velentium.android.platformv.ble.core.PlatformBleManager
import com.velentium.android.platformv.ble.core.common.BleConnectionStatus
import com.velentium.android.platformv.ble.core.common.BleManagerError
import com.velentium.android.platformv.utils.asHex
import com.velentium.android.platformv.utils.rx.simpleSubscribe
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import no.nordicsemi.android.ble.data.Data

/**
 * Basic example implementation of a subclass of [PlatformBleManager].
 * This is a Nordic UART Service sample, [UARTBleManager].
 * It is useful since many projects start off using NUS, when in the prototyping phase.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class UARTBleManager(context: Context) : PlatformBleManager(context) {

    protected val compositeDisposable = CompositeDisposable()

    //region Properties
    var rxCharacteristic: BluetoothGattCharacteristic? = null
        protected set

    var txCharacteristic: BluetoothGattCharacteristic? = null
        protected set

    var useLongWrite = true
        protected set

    protected var incomingDataProcessor = PublishProcessor.create<Data>()

    val incomingDataFlowable: Flowable<Data>
        get() = incomingDataProcessor
            .filter { it.value != null }


    //endregion

    //region Lifecycle / Overrides
    override fun getGattCallback(): BleManagerGattCallback {
        return UARTGattCallback()
    }

    override fun write(identifier: BluetoothGattCharacteristic, bytes: ByteArray): Single<Unit> {
        val rxIdentifier = rxCharacteristic
            ?: return Single.error(BleManagerError.DeviceNotConnectedError())
        if (!isConnected)
            return Single.error(BleManagerError.DeviceNotConnectedError())
        if (bytes.isEmpty())
            return Single.error(BleManagerError.GattWriteError("Data cannot be empty."))
        return Single.create { single ->
            val writeRequest = writeCharacteristic(rxIdentifier, bytes)
                .with { _, data ->
                    Log.i(
                        TAG,
                        "Successfully wrote to ${rxIdentifier.uuid} with data: ${data.value?.asHex}"
                    )
                    single.onSuccess(Unit)
                }
                .fail { _, status ->
                    val error = BleManagerError.GattWriteError(
                        "Failed to write to ${rxIdentifier.uuid}",
                        status
                    )
                    Log.e(TAG, error.toString(), error)
                    single.onError(error)
                }
            if (!useLongWrite) {
                // automatically split data into MTU-3-byte long packets
                writeRequest.split()
            }
            writeRequest.enqueue()
        }
    }

    fun send(bytes: ByteArray): Single<Unit> {
        val rxIdentifier = rxCharacteristic
            ?: return Single.error(BleManagerError.DeviceNotConnectedError())
        return write(rxIdentifier, bytes)
    }

    fun sendCommand(cmdData: Data) =
        cmdData.value?.takeIf { it.isNotEmpty() }?.let { sendCommand(it) }

    fun sendCommand(cmd: ByteArray) {
        Log.i(TAG, "Sending command: ${cmd.asHex}")
        compositeDisposable += send(cmd)
            .subscribe({
                Log.i(TAG, "Sent command: ${cmd.asHex}")
            }, { error ->
                Log.e(TAG, "Failed to send command: ${cmd.asHex}", error)
            })
    }

    private fun initializeRx() {
        compositeDisposable += connectionStatusFlowable
            .doOnNext {
                Log.i(TAG, "BleConnectionStatus is: $it")
            }
            .skipWhile { it is BleConnectionStatus.DISCONNECTED }
            .simpleSubscribe {
                Log.i(TAG, "UARTBleManger is connected to: $it")
            }
    }

    //endregion

    //region Nested Classes

    private inner class UARTGattCallback : BleManagerGattCallback() {
        /**
         * This method is called from the main thread when the services has been discovered and
         * the device is supported (has required service).
         */
        override fun initialize() {
            super.initialize()
            initializeRx()
            Log.i(TAG, "UART Ble Device is initializing...")
            negotiateMtuSize()
            incomingDataProcessor = PublishProcessor.create()
            txCharacteristic?.let {
                setUpNotificationListener(identifier = it, onNotified = { _, data: Data ->
                    Log.d(
                        TAG,
                        "Incoming UART data is ${data.value?.size ?: 0} bytes:\n${data.value?.asHex}"
                    )
                    incomingDataProcessor.onNext(data)
                })
            } ?: kotlin.run {
                Log.e(TAG, "Failed to setup incoming listener")
            }
        }

        override fun onDeviceReady() {
            super.onDeviceReady()
            Log.i(TAG, "UART Ble Device is ready...")
            currentDevice?.let {
                connectionStatusRelay.accept(BleConnectionStatus.CONNECTED(it))
            }
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            var writeRequest = false
            var writeCommand = false
            gatt.getService(BleConstants.UART_SERVICE_UUID)?.let { service ->
                rxCharacteristic =
                    service.getCharacteristic(BleConstants.UART_RX_CHARACTERISTIC_UUID)
                txCharacteristic =
                    service.getCharacteristic(BleConstants.UART_TX_CHARACTERISTIC_UUID)

                rxCharacteristic?.let { characteristic ->
                    writeRequest = (characteristic.properties
                            and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0
                    writeCommand = (characteristic.properties
                            and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0
                    if (writeRequest) {
                        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    } else {
                        useLongWrite = false
                    }
                }
            }

            val hasServices = (rxCharacteristic != null &&
                    txCharacteristic != null &&
                    (writeRequest || writeCommand))
            Log.d(TAG, "UART Services supported: $hasServices")
            return hasServices
        }

        override fun onServicesInvalidated() {
            Log.w(TAG, "Service invalided, connected: $isDeviceConnected")
            rxCharacteristic = null
            txCharacteristic = null
            useLongWrite = true
            incomingDataProcessor.onComplete()

            if (isDeviceConnected) {
                disconnect()
            }
            handleDisconnection()
        }
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = UARTBleManager::class.java.simpleName
    }

    //endregion
}