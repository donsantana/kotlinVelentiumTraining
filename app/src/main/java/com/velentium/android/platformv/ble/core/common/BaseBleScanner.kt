package com.velentium.android.platformv.ble.core.common

import android.os.ParcelUuid
import com.velentium.android.platformv.ble.core.BleConstants
import io.reactivex.Flowable
import no.nordicsemi.android.support.v18.scanner.ScanResult

interface BaseBleScanner {
    val isScanning: Boolean

    fun startScanning(
        serviceUuid: ParcelUuid? = null,
        name: String? = null,
        reportDelay: Long = 2500L,
        timeout: Long = BleConstants.SCAN_TIMEOUT
    ): Flowable<Set<ScanResult>>

    fun stopScanning()
}