package com.velentium.android.platformv.storage.product

import android.content.Context
import android.util.Log
import com.velentium.android.platformv.storage.core.RealmManager
import io.realm.Realm
import io.realm.RealmConfiguration

@Suppress("unused")
class PlatformRealmManager : RealmManager() {

    override fun initialize(
        context: Context,
        schemaVersion: Long,
        deleteIfMigrationNeeded: Boolean
    ) {
        super.initialize(context, schemaVersion, deleteIfMigrationNeeded)
        // TODO: initialize Realm objects as needed
        Log.i(TAG, "$TAG initialized...")
    }

    override fun resetRealm(realmConfiguration: RealmConfiguration) {
        super.resetRealm(realmConfiguration)
        Log.i(TAG, "Realm reset...")
    }

    override fun getDefault(context: Context): Realm {
        Log.i(TAG, "Getting default realm...")
        return super.getDefault(context)
    }

    companion object {
        val TAG: String = PlatformRealmManager::class.java.simpleName
    }
}