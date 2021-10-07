@file:Suppress("ThrowableNotThrown")

package com.velentium.android.platformv.ble.core

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.velentium.android.platformv.ble.core.common.*
import com.velentium.android.platformv.ble.core.common.BaseBleManager.Companion.statusString
import com.velentium.android.platformv.ble.product.UARTBleManager
import com.velentium.android.platformv.utils.asHex
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.ReplaySubject
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * This is the base BleManager class for Platform V
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PlatformBleManager(
    context: Context,
    override val bleScanner: BaseBleScanner = PlatformBleScanner()
) : BleManager(context),
    BaseBleScanner by bleScanner,
    BaseBleManager {

    //region Properties

    override val isBluetoothEnabled: Boolean
        get() = BluetoothAdapter.getDefaultAdapter().isEnabled

    override var mtuSize: Int = BleConstants.DEFAULT_MTU_SIZE
        protected set

    override val isDeviceConnected: Boolean
        get() = isConnected

    override val isDeviceBonded: Boolean
        get() = isBonded

    override val currentDeviceMacAddress: String?
        get() = currentDevice?.address

    override val currentDevice: BluetoothDevice?
        get() = this.bluetoothDevice

    override val activeNotificationListeners = mutableSetOf<BluetoothGattCharacteristic>()

    protected var connectionStatusRelay: BehaviorRelay<BleConnectionStatus> =
        BehaviorRelay.createDefault(BleConnectionStatus.DISCONNECTED())
    override val connectionStatusFlowable: Flowable<BleConnectionStatus>
        get() = connectionStatusRelay
            .distinctUntilChanged()
            .debounce(500L, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.LATEST)

    val handler = Handler(Looper.getMainLooper())

    protected var disconnectRequested = false

    protected val adapterStateSubject = ReplaySubject.create<BleAdapterState>(1)

    override val adapterStateObservable: Observable<BleAdapterState>
        get() = adapterStateSubject.distinctUntilChanged()

    protected val bleAdapterStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent
                ?.takeIf { it.action == BluetoothAdapter.ACTION_STATE_CHANGED }
                ?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                ?.takeIf { it != BluetoothAdapter.ERROR }
                ?: kotlin.run {
                    Log.w(PlatformBleService.TAG, "No action with intent!")
                    return
                }
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.i(PlatformBleService.TAG, "Ble Adapter is off...")
                    adapterStateSubject.onNext(BleAdapterState.OFF)
                }
                BluetoothAdapter.STATE_ON -> {
                    Log.i(PlatformBleService.TAG, "Ble Adapter is on...")
                    adapterStateSubject.onNext(BleAdapterState.ON)
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    Log.i(PlatformBleService.TAG, "Ble Adapter turning off...")
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    Log.i(PlatformBleService.TAG, "Ble Adapter turning on...")
                }
            }
        }
    }

    //endregion

    //region Lifecycle/Overrides

    override fun enableBluetooth(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (!adapter.isEnabled) {
            adapter.enable()
            return true
        }
        return false
    }

    override fun disableBluetooth(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter.isEnabled) {
            adapter.disable()
            return false
        }
        return true
    }

    override fun restartBluetooth(): Single<Unit> {
        return Single
            .create<Boolean> { single ->
                disableBluetooth()
                handler.postDelayed({
                    single.onSuccess(enableBluetooth())
                }, 2000L) // wait a couple ticks then re-enable it
            }
            .flatMap { _ ->
                adapterStateSubject
                    .doOnNext {
                        Log.i(TAG, "BLE Adapter state is: $it")
                    }
                    .skipWhile { it != BleAdapterState.ON }
                    .take(1)
                    .firstOrError()
                    .map { Unit }
            }
            .timeout(10000L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
    }

    override fun registerAdapterStateReceiver(context: Context) {
        val intentFilter = IntentFilter().also {
            it.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        Log.d(TAG, "Ble adapter state receiver registered.")
        context.registerReceiver(bleAdapterStateReceiver, intentFilter)
    }

    override fun unregisterAdapterStateReceiver(context: Context, close: Boolean) {
        Log.d(TAG, "Ble adapter state receiver unregistered.")
        context.unregisterReceiver(bleAdapterStateReceiver)
        if (close) {
            this.close()
        }
    }

    override fun promptToEnableBluetooth(activity: Activity) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLED)
    }

    override fun negotiateMtuSize(requestedSize: Int) {
        requestMtu(requestedSize)
            .with { device, mtu ->
                Log.i(TAG, "Negotiated mtu: $mtu, for device: $device")
                mtuSize = mtu
            }
            .enqueue()
    }

    override fun connectDevice(device: BluetoothDevice): Single<BluetoothDevice> {
        if (isDeviceConnected && device.address == currentDevice?.address) {
            Log.v(TAG, "Already connected to device: $device")
            return Single.just(currentDevice)
        }
        Log.v(TAG, "Connecting to device: $device")
        return Single.create { single ->
            connect(device)
                .done {
                    Log.i(TAG, "Connected to device: $it")
                    single.onSuccess(it)
                }
                .fail { _, status ->
                    val exception = BleManagerError.DeviceConnectionError(
                        "Failed to connect to device: ",
                        status
                    )
                    Log.e(TAG, exception.toString(), exception)
                    single.onError(exception)
                }
                .enqueue()
        }
    }

    override fun disconnectDevice(): Single<Unit> {
        if (!isConnected) {
            return Single.just(Unit)
        }
        disconnectRequested = true
        return Single.create { single ->
            disconnect()
                .done {
                    Log.i(TAG, "Disconnected from device: $currentDevice")
                    single.onSuccess(Unit)
                }
                .fail { _, status ->
                    val exception = BleManagerError.DeviceDisconnectionError(
                        "Failed to disconnect to device.",
                        status
                    )
                    Log.e(TAG, exception.toString(), exception)
                    single.onError(exception)
                }
                .enqueue()
        }
    }

    override fun setUpNotificationListener(
        identifier: BluetoothGattCharacteristic,
        onNotified: (BluetoothGattCharacteristic, Data) -> Unit,
        onEnabled: ((Boolean, BleManagerError?) -> Unit)?
    ) {
        if (!isDeviceConnected) {
            onEnabled?.invoke(false, BleManagerError.DeviceNotConnectedError())
            return
        }
        if (activeNotificationListeners.contains(identifier)) {
            onEnabled?.invoke(true, null)
            return
        }
        setNotificationCallback(identifier)
            .with { _, data ->
                onNotified(identifier, data)
            }
        enableNotifications(identifier)
            .done {
                Log.i(TAG, "Enabled notifications for: ${identifier.uuid}")
                activeNotificationListeners.add(identifier)
                onEnabled?.invoke(true, null)
            }
            .fail { _, status ->
                val exception =
                    BleManagerError.GattEnableNotificationError(
                        "Failed to enable notifications for: ${identifier.uuid}, with status: $status"
                    )
                Log.e(TAG, exception.toString(), exception)
                onEnabled?.invoke(false, exception)
            }
            .enqueue()
    }

    override fun read(identifier: BluetoothGattCharacteristic): Single<Data> {
        return Single.create { single ->
            readCharacteristic(identifier)
                .with { _, data ->
                    Log.i(TAG, "Read bytes: ${data.value?.asHex}, from: ${identifier.uuid}")
                    single.onSuccess(data)
                }
                .done {
                    Log.i(TAG, "Finished reading from: ${identifier.uuid}")
                }
                .fail { _, status ->
                    val error = BleManagerError.GattReadError(
                        "Failed to read from ${identifier.uuid}",
                        status
                    )
                    Log.e(TAG, error.toString(), error)
                    single.onError(error)
                }
                .enqueue()
        }
    }

    override fun write(identifier: BluetoothGattCharacteristic, bytes: ByteArray): Single<Unit> {
        if (!isConnected)
            return Single.error(BleManagerError.GattWriteError("Data cannot be empty."))
        if (bytes.isEmpty())
            return Single.error(BleManagerError.GattWriteError("Data cannot be empty."))

        return Single.create { single ->
            writeCharacteristic(identifier, bytes)
                .split()
                .with { _, data ->
                    Log.i(
                        TAG,
                        "Successfully wrote to ${identifier.uuid} with data: ${data.value?.asHex}"
                    )
                    single.onSuccess(Unit)
                }
                .fail { _, status ->
                    val error = BleManagerError.GattWriteError("Failed to write to ${identifier.uuid}", status)
                    Log.e(UARTBleManager.TAG, error.toString(), error)
                    single.onError(error)
                }
                .enqueue()
        }
    }

    override fun write(identifier: BluetoothGattCharacteristic, data: Data): Single<Unit> {
        val bytes = data.value
            ?: return Single.error(BleManagerError.GattWriteError("Data cannot be empty."))
        return write(identifier = identifier, bytes = bytes)
    }

    override fun write(
        identifier: BluetoothGattCharacteristic,
        value: String,
        charset: Charset
    ): Single<Unit> {
        val bytes = value.trim().toByteArray(charset).takeIf { it.isNotEmpty() }
            ?: return Single.error(BleManagerError.GattWriteError("Data cannot be empty."))
        return write(identifier = identifier, bytes = bytes)
    }

    override fun stopListener(identifier: BluetoothGattCharacteristic) {
        if (!activeNotificationListeners.contains(identifier)) {
            Log.w(TAG, "$identifier does not have a notification listener.")
            return
        }
        removeNotificationCallback(identifier)
        disableNotifications(identifier)
            .done {
                Log.i(TAG, "Stopped listener for: $identifier")
            }
            .fail { _, status ->
                Log.e(
                    TAG,
                    "Failed to stop listener for $identifier, with status: ${
                        statusString(status)
                    }"
                )
            }
            .enqueue()
    }

    override fun stopAllListeners() {
        activeNotificationListeners.forEach {
            stopListener(it)
        }
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return DefaultGattCallback()
    }

    //endregion

    //region Protected Methods

    protected fun handleDisconnection() {
        val currentStatus = connectionStatusRelay.value
        if (currentStatus is BleConnectionStatus.DISCONNECTED) {
            Log.v(TAG, "Relay already set to disconnected: ${currentStatus.isExpected}")
        } else {
            connectionStatusRelay.accept(BleConnectionStatus.DISCONNECTED(disconnectRequested))
        }
        disconnectRequested = false // reset it here
    }

    //endregion

    //region Nested Classes

    private inner class DefaultGattCallback : BleManagerGattCallback() {
        /**
         * This method is called from the main thread when the services has been discovered and
         * the device is supported (has required service).
         */
        override fun initialize() {
            super.initialize()
            negotiateMtuSize(requestedSize = BleConstants.DEFAULT_MTU_SIZE)
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            return true
        }

        override fun onDeviceReady() {
            super.onDeviceReady()
            currentDevice?.let {
                connectionStatusRelay.accept(BleConnectionStatus.CONNECTED(it))
            }
        }

        override fun onServicesInvalidated() {
            // perform cleanup
            Log.w(TAG, "Service invalided, connected: $isDeviceConnected")
            mtuSize = BleConstants.DEFAULT_MTU_SIZE
            activeNotificationListeners.clear()

            if (isDeviceConnected) {
                disconnect()
            }
            handleDisconnection()
        }
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = PlatformBleManager::class.java.simpleName
        const val REQUEST_BLUETOOTH_ENABLED: Int = 10101010
    }

    //endregion
}