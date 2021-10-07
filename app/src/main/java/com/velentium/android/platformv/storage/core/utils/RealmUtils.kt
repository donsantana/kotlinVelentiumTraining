package com.velentium.android.platformv.storage.core.utils

import android.util.Log
import com.velentium.android.platformv.storage.core.RealmManager
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.exceptions.RealmMigrationNeededException
import io.realm.kotlin.deleteFromRealm

/**
 * Deletes the object from realm
 */
fun <T : RealmModel> T.delete(realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration) {
    val realm = Realm.getInstance(realmConfiguration)
    try {
        realm.beginTransaction()
        this.deleteFromRealm()
        realm.commitTransaction()
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        throw e
    } finally {
        realm.close()
    }
}

fun Realm.safeClose() {
    if (this.isClosed) {
        // do nothing
        Log.i("Realm", "safeClose: already closed")
    } else {
        if (this.isInTransaction) {
            this.cancelTransaction()
        }
        this.close()
    }
}

/**
 * Update params of a Realm Model. It will also create the object if it doesn't
 * exist. If you have a realm managed object or need the object use [save] or
 * [update]
 */
fun <E : RealmModel?> createOrUpdate(
    realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration,
    clazz: Class<E>,
    body: (E) -> Unit
) {
    var realm: Realm? = null
    try {
        realm = Realm.getInstance(realmConfiguration)
        realm.beginTransaction()
        var realmObject = realm.where(clazz).findFirst()
        if (realmObject == null) {
            realmObject = realm.createObject(clazz)
        }

        body.invoke(realmObject!!)

        realm.commitTransaction()
    } catch (e: IllegalStateException) {
        e.printStackTrace()
    } catch (e: RealmMigrationNeededException) {
        e.printStackTrace()
    } finally {
        realm!!.safeClose()
    }
}

/**
 * Deletes the object from realm
 */
fun <T : RealmModel> Realm.safeDeleteWithoutClosing(clazz: Class<T>) {
    try {
        beginTransaction()
        delete(clazz)
        commitTransaction()
    } catch (e: Throwable) {
        if (isInTransaction) {
            cancelTransaction()
        }
        throw e
    }
}