package com.velentium.android.platformv.ui.viewmodels.base

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.velentium.android.platformv.app.PermissionRequestHandler
import com.velentium.android.platformv.app.PermissionRequester
import com.velentium.android.platformv.app.PlatformApplication
import com.velentium.android.platformv.ble.core.BleConstants
import com.velentium.android.platformv.ble.core.common.BleManagerError
import com.velentium.android.platformv.ble.core.common.BleService
import com.velentium.android.platformv.ble.core.parcelUuid
import com.velentium.android.platformv.ui.viewmodels.dto.BleScanResult
import com.velentium.android.platformv.utils.rx.safeDispose
import com.velentium.android.platformv.utils.rx.simpleSubscribe
import com.velentium.android.platformv.utils.ui.LiveDataEvent
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.ReplaySubject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
/**
 * Base class for any [AndroidViewModel] that needs to have access to the [BleService]
 */
open class PlatformConnectedViewModel(
    application: Application,
    protected val permissionHandler: PermissionRequester = PermissionRequestHandler
) : AndroidViewModel(application) {

    //region Properties

    protected var bleService: BleService? = null
    protected var compositeDisposable = CompositeDisposable()
    protected var serviceInitialized = MutableLiveData<LiveDataEvent<Boolean>>()
    val onServiceInitialized: LiveData<LiveDataEvent<Boolean>>
        get() = serviceInitialized

    protected var connectedDevice = MutableLiveData<Result<BluetoothDevice>>()
    protected var scanResults = MutableLiveData<LiveDataEvent<Set<BleScanResult>>>()
    protected val dateFormat = SimpleDateFormat("MM/dd/yyyy h:mm:ss a", Locale.getDefault())
    protected var isConnecting = false

    protected val permissionGrantedSubject = ReplaySubject.create<Boolean>(1)
    protected val requiredPermissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val platformApp: PlatformApplication
        get() = getApplication()

    val isScanning: Boolean
        get() = bleService?.isScanning ?: false

    val isConnected: Boolean
        get() = bleService?.isDeviceConnected ?: false

    val bleServiceInitializedSingle: Single<BleService>
        get() = platformApp.bleServiceSingle
            .flatMap { service ->
                permissionGrantedSubject
                    .take(1)
                    .firstOrError()
                    .map {
                        if (it) service
                        else throw BleManagerError.BlePermissionsError("Failed to get permissions required for Bluetooth")
                    }
            }

    //endregion

    //region Lifecycle

    init {
        Log.i(TAG, "Initialized $TAG")
        compositeDisposable += bleServiceInitializedSingle
            .subscribe { service ->
                Log.i(TAG, "BleService has been initialized.")
                bleService = service
                serviceInitialized.postValue(LiveDataEvent(bleService != null))
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.safeDispose()
        scanResults = MutableLiveData()
        bleService = null
    }

    //endregion

    //region Public Methods

    fun promptForPermissions(activity: Activity) {
        val requests: List<Single<Pair<String, Boolean>>> = requiredPermissions
            .filter {
                !permissionHandler.hasPermission(activity, it)
            }
            .map {
                permissionHandler.requestPermission(
                    activity = activity,
                    permission = it,
                    requestCode = kotlin.math.abs(it.hashCode())
                )
            }
        if (requests.isEmpty()) {
            Log.i(TAG, "All required permissions already granted!")
            if (!permissionHandler.isDozeModeDisabled(getApplication())) {
                val requestSent = permissionHandler.checkAndPromptIfDozeModeEnabled(getApplication())
                permissionGrantedSubject.onNext(requestSent)
            } else {
                permissionGrantedSubject.onNext(true)
            }
            return
        }
        val responses = mutableListOf<Boolean>()
        compositeDisposable += Single.concat(requests)
            .subscribe({ result ->
                Log.i(TAG, "Permission: ${result.first}, granted: ${result.second}")
                responses.add(result.second)
                if (responses.count() == requests.count() - 1) { // -1 cause location request have only 1 prompt
                    val allGranted = responses.all { it }
                    if (!permissionHandler.isDozeModeDisabled(getApplication())) {
                        val requestSent = permissionHandler.checkAndPromptIfDozeModeEnabled(getApplication())
                        permissionGrantedSubject.onNext(allGranted && requestSent)
                    } else {
                        permissionGrantedSubject.onNext(allGranted)
                    }
                }
            }, { error ->
                Log.e(TAG, "Failed to get one or more permissions granted, with error: $error")
            })
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    /**
     * Attempt to connect to the given [device].
     */
    fun connect(device: BluetoothDevice): LiveData<Result<BluetoothDevice>> {
        if (isConnecting) {
            return connectedDevice
        }
        connectedDevice = MutableLiveData()
        val service = bleService ?: kotlin.run {
            @Suppress("ThrowableNotThrown")
            connectedDevice.value =
                Result.failure(BleManagerError.DeviceNotConnectedError("Ble device is not connected!"))
            return connectedDevice
        }
        isConnecting = true
        compositeDisposable += service.connectDevice(device)
            .subscribe({
                Log.i(TAG, "Successfully connected to device: $device")
                isConnecting = false
                connectedDevice.value = Result.success(it)
            }, { error ->
                Log.e(TAG, "Failed to connect to device with error: $error")
                isConnecting = false
                connectedDevice.value = Result.failure(error)
            })
        return connectedDevice
    }

    /**
     * Disconnects if there is a connected device.
     */
    fun disconnect() {
        val service = bleService
            ?.takeIf { it.isDeviceConnected }
            ?: kotlin.run {
                Log.w(TAG, "No device connected.")
                return
            }
        compositeDisposable += service.disconnectDevice()
            .simpleSubscribe {
                Log.i(TAG, "Disconnected from device!")
            }
    }

    /**
     * Start scanning for BLE devices, and send results to [scanResults],
     * for consumption by UI.
     */
    fun startScanning(): LiveData<LiveDataEvent<Set<BleScanResult>>> {
        scanResults = MutableLiveData()
        Log.i(
            TAG,
            "Started scanning for devices with service: ${BleConstants.UART_SERVICE_UUID}"
        )
        compositeDisposable += bleServiceInitializedSingle
            .toFlowable()
            .flatMap { service ->
                Log.i(TAG, "Starting scan...")
                service.startScanning(serviceUuid = BleConstants.UART_SERVICE_UUID.parcelUuid)
            }
            .timeout(
                BleConstants.SCAN_TIMEOUT + 2000L,
                TimeUnit.SECONDS,
                AndroidSchedulers.mainThread()
            )
            .subscribe({ results ->
                Log.i(TAG, "Found devices with service: $results")
                if (results.size != scanResults.value?.peekValue?.size) {
                    val bleResults =
                        results.map { BleScanResult(it, dateFormat.format(Date())) }.toSet()
                    scanResults.value = LiveDataEvent(bleResults)
                }
            }, { error ->
                Log.e(TAG, "Failed to get scan results", error)
                scanResults.value = LiveDataEvent(setOf())
            })
        return scanResults
    }

    fun stopScanning() {
        bleService?.stopScanning()
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = PlatformConnectedViewModel::class.java.simpleName
    }

    //endregion
}