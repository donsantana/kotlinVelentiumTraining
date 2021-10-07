package com.velentium.android.platformv.ble.core

import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.velentium.android.platformv.ble.core.common.BaseBleScanner
import com.velentium.android.platformv.ble.core.common.BleManagerError
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import no.nordicsemi.android.support.v18.scanner.*

/**
 * This is the base BleScanner class for Platform V
 */
class PlatformBleScanner : BaseBleScanner {
    // TODO: Implement BroadcastReceiver code to get background scanning working
    // TODO: possibly provide a Flow implementation as-well / instead-of Rx

    //region Properties

    private val scanResults = mutableMapOf<String, ScanResult>()
    private val scanner: BluetoothLeScannerCompat
        get() = BluetoothLeScannerCompat.getScanner()
    private lateinit var scanResultSubject: BehaviorSubject<Set<ScanResult>>
    private var handler = Handler(Looper.getMainLooper())

    override var isScanning = false
        private set

    private val scanTimeoutRunnable = Runnable {
        Log.w(TAG, "Scan timeout has occurred!")
        stopScanning()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i(TAG, "Scan Result: $result")
            if (!scanResults.containsKey(result.device.address)) {
                scanResults[result.device.address] = result
                scanResultSubject.onNext(scanResults.values.toSet())
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            Log.i(TAG, "Batched Scan Results: $results")
            var added = false
            results.forEach {
                added = !scanResults.containsKey(it.device.address)
                scanResults[it.device.address] = it
            }
            if (added) {
                scanResultSubject.onNext(scanResults.values.toSet())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val exception = BleManagerError.BleScannerError(errorCode)
            Log.e(TAG, "Scanning failed with exception", exception)
            scanResultSubject.onError(exception)
        }
    }

    //endregion

    //region Public Methods

    /**
     * Starts scanning for BLE devices with [serviceUuid], and/or [name].
     * If [timeout] is specified the scan will stop after [timeout] milliseconds.
     */
    override fun startScanning(
        serviceUuid: ParcelUuid?,
        name: String?,
        reportDelay: Long,
        timeout: Long
    ): Flowable<Set<ScanResult>> {
        if (isScanning) {
            // clears scanResults
            stopScanning()
        } else {
            scanResults.clear()
        }
        scanResultSubject = BehaviorSubject.create()
        val filters = arrayListOf<ScanFilter>()
        serviceUuid?.let {
            filters.add(ScanFilter.Builder().setServiceUuid(it).build())
        }
        name?.let {
            filters.add(ScanFilter.Builder().setDeviceName(it).build())
        }
        val settingsBuilder = ScanSettings.Builder()
            .setLegacy(true)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)

        if (reportDelay > 0L) {
            settingsBuilder
                .setReportDelay(reportDelay)
                .setUseHardwareBatchingIfSupported(false)
        }
        val settings = settingsBuilder.build()
        scanner.startScan(filters, settings, scanCallback)
        handler.postDelayed(scanTimeoutRunnable, timeout)
        isScanning = true
        return scanResultSubject
            .toFlowable(BackpressureStrategy.LATEST)
    }

    override fun stopScanning() {
        if (isScanning) {
            handler.removeCallbacks(scanTimeoutRunnable)
            Log.i(TAG, "Stopped scanning...")
            scanner.stopScan(scanCallback)
            isScanning = false
            scanResultSubject.onNext(scanResults.values.toSet())
            scanResults.clear()
        }
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = PlatformBleScanner::class.java.simpleName
    }

    //endregion
}