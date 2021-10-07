package com.velentium.android.platformv.ble.core

import android.app.Activity
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import com.velentium.android.platformv.ble.core.common.*
import com.velentium.android.platformv.ui.MainActivity
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.nio.charset.Charset

/**
 * This provides a base class for an Android BLE service, for Platform V.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class PlatformBleService :
    Service(),
    BleService,
    BaseBleManager,
    BaseBleScanner,
    ForegroundService {

    //region Properties

    override lateinit var bleManager: BaseBleManager

    override val bleScanner: BaseBleScanner
        get() = bleManager.bleScanner

    override val mtuSize: Int
        get() = bleManager.mtuSize

    override val isBluetoothEnabled: Boolean
        get() = bleManager.isBluetoothEnabled

    override val activeNotificationListeners: Set<BluetoothGattCharacteristic>
        get() = bleManager.activeNotificationListeners

    override val connectionStatusFlowable: Flowable<BleConnectionStatus>
        get() = bleManager.connectionStatusFlowable

    override val isScanning: Boolean
        get() = bleManager.bleScanner.isScanning

    override lateinit var foregroundServiceHandler: ForegroundServiceHandler

    override val currentDevice: BluetoothDevice?
        get() = bleManager.currentDevice

    override val currentDeviceMacAddress: String?
        get() = bleManager.currentDeviceMacAddress

    override val isDeviceConnected: Boolean
        get() = bleManager.isDeviceConnected

    override val isDeviceBonded: Boolean
        get() = bleManager.isDeviceBonded

    override val adapterStateObservable: Observable<BleAdapterState>
        get() = bleManager.adapterStateObservable


    //endregion

    //region Lifecycle

    override fun initializeBleManager() {
        bleManager = PlatformBleManager(this)

        // derived classes could override this method,
        // and initialize a different/project specific ble manager as needed
        // i.e., UARTBleManager
//         bleManager = UARTBleManager(this)
    }

    override fun registerAdapterStateReceiver(context: Context) =
        bleManager.registerAdapterStateReceiver(this)

    override fun unregisterAdapterStateReceiver(context: Context, close: Boolean) =
        bleManager.unregisterAdapterStateReceiver(this, close)

    override fun enableBluetooth(): Boolean =
        bleManager.enableBluetooth()

    override fun disableBluetooth(): Boolean =
        bleManager.disableBluetooth()

    override fun restartBluetooth(): Single<Unit> =
        bleManager.restartBluetooth()

    override fun onCreate() {
        super.onCreate()
        initializeBleManager()
        foregroundServiceHandler = AndroidForegroundServiceHandler(
            service = this,
            activityClass = MainActivity::class.java
        )
        bleManager.registerAdapterStateReceiver(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying service...")
        dispose()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "Task Removed...")
        dispose()
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "PlatformBleService has been bound.")
        return ServiceBinder()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "PlatformBleService has been unbound.")
        return super.onUnbind(intent)
    }

    override fun startForegroundNotification() {
        foregroundServiceHandler.startForegroundNotification()
    }

    override fun stopForegroundNotification() {
        foregroundServiceHandler.stopForegroundNotification()
    }

    override fun startScanning(
        serviceUuid: ParcelUuid?,
        name: String?,
        reportDelay: Long,
        timeout: Long
    ): Flowable<Set<ScanResult>> {
        return bleManager.bleScanner.startScanning(
            serviceUuid = serviceUuid,
            name = name,
            reportDelay = reportDelay,
            timeout = timeout
        )
    }

    override fun stopScanning() {
        bleManager.bleScanner.stopScanning()
    }

    override fun promptToEnableBluetooth(activity: Activity) {
        bleManager.promptToEnableBluetooth(activity)
    }

    override fun negotiateMtuSize(requestedSize: Int) {
        bleManager.negotiateMtuSize(requestedSize)
    }

    override fun connectDevice(device: BluetoothDevice): Single<BluetoothDevice> {
        return bleManager.connectDevice(device)
    }

    override fun disconnectDevice(): Single<Unit> {
        return bleManager.disconnectDevice()
    }

    override fun setUpNotificationListener(
        identifier: BluetoothGattCharacteristic,
        onNotified: (BluetoothGattCharacteristic, Data) -> Unit,
        onEnabled: ((Boolean, BleManagerError?) -> Unit)?
    ) {
        bleManager.setUpNotificationListener(identifier, onNotified, onEnabled)
    }

    override fun stopListener(identifier: BluetoothGattCharacteristic) {
        bleManager.stopListener(identifier)
    }

    override fun stopAllListeners() {
        bleManager.stopAllListeners()
    }

    override fun read(identifier: BluetoothGattCharacteristic): Single<Data> {
        return bleManager.read(identifier)
    }

    override fun write(identifier: BluetoothGattCharacteristic, data: Data): Single<Unit> {
        return bleManager.write(identifier, data)
    }

    override fun write(identifier: BluetoothGattCharacteristic, bytes: ByteArray): Single<Unit> {
        return bleManager.write(identifier, bytes)
    }

    override fun write(
        identifier: BluetoothGattCharacteristic,
        value: String,
        charset: Charset
    ): Single<Unit> {
        return bleManager.write(identifier, value, charset)
    }

    //endregion

    //region Private Methods

    protected fun dispose() {
        bleManager.unregisterAdapterStateReceiver(this)
        foregroundServiceHandler.stopForegroundNotification()
    }

    //endregion

    //region Nested Classes

    inner class ServiceBinder : IBinder, Binder() {
        val service: BleService = this@PlatformBleService
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = PlatformBleService::class.java.simpleName
    }

    //endregion
}