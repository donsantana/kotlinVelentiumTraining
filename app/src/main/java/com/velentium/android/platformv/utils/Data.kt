package com.velentium.android.platformv.utils

import no.nordicsemi.android.ble.data.Data

val Data.asHex: String
    get() = this.value?.asHex ?: ""