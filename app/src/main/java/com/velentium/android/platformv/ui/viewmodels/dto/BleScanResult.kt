package com.velentium.android.platformv.ui.viewmodels.dto

import android.bluetooth.BluetoothDevice
import android.util.SparseArray
import com.google.gson.Gson
import no.nordicsemi.android.support.v18.scanner.ScanResult
import kotlin.math.abs

data class BleScanResult(
    val id: Int,
    val name: String,
    val address: String,
    val dateFound: String,
    val rssi: Int,
    val device: BluetoothDevice,
    val manufacturerSpecificData: SparseArray<ByteArray>?
) {
    constructor(scanResult: ScanResult, dateString: String) : this(
        id = abs(scanResult.device.address.hashCode()),
        name = scanResult.device.name,
        address = scanResult.device.address,
        dateFound = dateString,
        rssi = scanResult.rssi,
        device = scanResult.device,
        manufacturerSpecificData = scanResult.scanRecord?.manufacturerSpecificData
    )
}