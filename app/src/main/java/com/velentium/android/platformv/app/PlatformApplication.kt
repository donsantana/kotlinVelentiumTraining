package com.velentium.android.platformv.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.velentium.android.platformv.utils.rx.simpleSubscribe
import com.velentium.android.platformv.ble.core.PlatformBleService
import com.velentium.android.platformv.ble.core.common.BleService
import com.velentium.android.platformv.di.networkModule
import com.velentium.android.platformv.di.viewModelModule
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Base application class for Platform V
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class PlatformApplication : Application(), ForegroundStateHandler {

    //region Properties

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    open val bleServiceClass: Class<*>
        get() {
            // Derived classes should override this method,
            // and return an application specific service class.
            return PlatformBleService::class.java
        }

    var bleService: BleService? = null
        protected set

    protected var bleServiceSubject = ReplaySubject.create<BleService>(1)
    val bleServiceSingle: Single<BleService>
        get() = bleServiceSubject
            .take(1)
            .firstOrError()
            .doOnSuccess {
                Log.i(TAG, "Ble Service is: ${it::class.java.simpleName}")
            }

    protected var isInForeground = AtomicBoolean(false)
    override val isForeground: Boolean
        get() = isInForeground.get()

    protected val bleServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.v(TAG, "Connected from BleService")
            bleService = (service as PlatformBleService.ServiceBinder).service
            bleService?.let { bleServiceSubject.onNext(it) }
            updateForegroundState()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.v(TAG, "Disconnected from BleService")
            bleServiceSubject = ReplaySubject.create(1)
            bleService = null
        }
    }

    //endregion

    //region Lifecycle

    override fun onCreate() {
        super.onCreate()
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.wtf(TAG, "Uncaught exception: ", throwable)
            defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        startBleService()
        initializeDI()
//        AWSMobileClient.getInstance().initialize(this, object : Callback<UserStateDetails> {
//            override fun onResult(result: UserStateDetails?) {
//                Log.d(TAG, "User state details are: $result")
//            }
//
//            override fun onError(exception: Exception?) {
//                val error = exception ?: kotlin.run {
//                    Log.e(TAG, "AWSMobileClient init failed, with unknown error.")
//                    return
//                }
//                Log.e(TAG, "AWSMobileClient init failed", error)
//            }
//        })
    }

    override fun onMoveToForeground() {
        Log.d(TAG, "App moved to foreground...")
        isInForeground.set(true)
        updateForegroundState()
    }

    override fun onMoveToBackground() {
        Log.d(TAG, "App moved to background...")
        isInForeground.set(false)
        updateForegroundState()
    }

    //endregion

    //region Public Methods

    /**
     * Should be called when the [BleService] needs to be unbound.
     */
    open fun onDestroy() {
        try {
            bleService?.let { service ->
                if (service.isDeviceConnected) {
                    service.disconnectDevice().simpleSubscribe {
                        Log.i(TAG, "Disconnected from device.")
                    }
                }
                service.stopForegroundNotification()
                Log.d(TAG, "Shutting down BleService")
                unbindService(bleServiceConnection)
            }
        } catch(ex: Throwable) {
            Log.e(TAG, "Unbinding service failed!")
        } finally {
            bleService = null
        }
    }

    //endregion

    //region Protected Methods

    protected fun initializeDI() {
        startKoin {
            androidLogger()
            androidContext(this@PlatformApplication)
            modules(viewModelModule)
            modules(networkModule)
        }
    }

    protected fun startBleService() {
        val serviceIntent = Intent(this, bleServiceClass)
        bindService(serviceIntent, bleServiceConnection, Context.BIND_AUTO_CREATE)
    }

    protected fun updateForegroundState() {
        val service = bleService ?: return
        if (isForeground) {
            service.stopForegroundNotification()
        } else {
            service.startForegroundNotification()
        }
    }

    //endregion

    //region Companion

    companion object {
        val TAG: String = PlatformApplication::class.java.simpleName
    }

    //endregion
}