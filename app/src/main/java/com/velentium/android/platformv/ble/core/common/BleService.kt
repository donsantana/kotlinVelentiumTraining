package com.velentium.android.platformv.ble.core.common

enum class BleAdapterState {
    OFF, ON
}

interface BleService: BaseBleManager, BaseBleScanner, ForegroundService {
    val foregroundServiceHandler: ForegroundServiceHandler
    val bleManager: BaseBleManager

    fun initializeBleManager()
}