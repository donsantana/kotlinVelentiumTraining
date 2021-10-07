package com.velentium.android.platformv.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface PermissionRequester {
    val permissionsRequested: Map<String, Pair<Int, Int>>

    fun isDozeModeDisabled(context: Context): Boolean

    fun hasPermission(
        context: Context,
        permission: String
    ): Boolean

    fun requestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int
    ): Single<Pair<String, Boolean>>

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )

    fun checkAndPromptIfDozeModeEnabled(context: Context): Boolean
}

/**
 * Singleton object for handling permission requests in Android.
 */
object PermissionRequestHandler : PermissionRequester {
    const val TAG = "PermissionRequester"

    override val permissionsRequested = mutableMapOf<String, Pair<Int, Int>>()

    private val permissionRequestSubject = PublishSubject.create<Pair<String, Int>>()

    override fun hasPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int
    ): Single<Pair<String, Boolean>> {
        permissionsRequested[permission]
            ?.takeIf { it.second == PackageManager.PERMISSION_GRANTED }
            ?.let {
                Log.i(TAG, "Permission already granted.")
                return Single.just(Pair(permission, true))
            }
        permissionsRequested[permission] = Pair(requestCode, PackageManager.PERMISSION_DENIED)
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        return permissionRequestSubject
            .skipWhile { it.first != permission }
            .map { Pair(permission, it.second == PackageManager.PERMISSION_GRANTED) }
            .take(1)
            .firstOrError()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(
            TAG,
            "onRequestPermissionsResult called for ${permissions.joinToString(" ")}, with request code: $requestCode"
        )
        val permission: String = permissionsRequested.values
            .indexOfFirst {
                it.first == requestCode
            }
            .takeIf { it != -1 }
            ?.let { permissionsRequested.keys.elementAtOrNull(it) }
            ?: kotlin.run {
                Log.w(TAG, "Request code not found: $requestCode")
                return
            }
        val result: Int = grantResults.firstOrNull()
            ?.let {
                if (it == PackageManager.PERMISSION_GRANTED) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            } ?: PackageManager.PERMISSION_DENIED
        Log.v(TAG, "Permission: $permission, granted: ${result == PackageManager.PERMISSION_GRANTED}")
        permissionsRequested[permission] = Pair(requestCode, result)
        permissionRequestSubject.onNext(Pair(permission, result))
    }

    override fun isDozeModeDisabled(context: Context): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
    }

    @SuppressLint("BatteryLife")
    override fun checkAndPromptIfDozeModeEnabled(context: Context): Boolean {
        return if (!isDozeModeDisabled(context)) {
            var successfullyRequested = true
            try {
                Intent()
                    .apply {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    .also {
                        context.startActivity(it)
                    }
            } catch (ex: ActivityNotFoundException) {
                Log.e(TAG, "Failed to opt-out of doze mode", ex)
                successfullyRequested = false
            }
            successfullyRequested
        } else true
    }
}