package com.velentium.android.platformv.ui.viewmodels

import android.app.Application
import com.velentium.android.platformv.ui.viewmodels.base.PlatformConnectedViewModel

class ConnectionViewModel(application: Application) : PlatformConnectedViewModel(application) {

    //region Companion

    companion object {
        val TAG: String = ConnectionViewModel::class.java.simpleName
    }

    //endregion
}