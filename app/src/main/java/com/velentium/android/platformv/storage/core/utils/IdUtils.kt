package com.velentium.android.platformv.storage.core.utils

import com.velentium.android.platformv.storage.core.RealmManager
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel

/**
 * Finds the maximum value of the "id" field for the Realm Objects that have it. If the object does
 * not have an "id" field it will throw an exception
 */
fun <E : RealmModel?> getMaxId(
    realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration,
    clazz: Class<E>
): Int {
    var maxId = -1
    val realm = Realm.getInstance(realmConfiguration)
    try {
        val realmObject = realm.where(clazz).max("id")
        realmObject?.let {
            maxId = realmObject.toInt()
        }
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        e.printStackTrace()
    } finally {
        realm.close()
    }
    return maxId
}

/**
 * Finds the maximum value of the "id" field for the Realm Objects that have it. If the object does
 * not have an "id" field it will throw an exception
 */
fun <E : RealmModel?> getMaxIdWithoutClosing(
    realm: Realm,
    clazz: Class<E>
): Int {
    var maxId = -1
    try {
        val realmObject = realm.where(clazz).max("id")
        realmObject?.let {
            maxId = realmObject.toInt()
        }
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        e.printStackTrace()
    }
    return maxId
}

/**
 * Finds the maximum value of the "id" +1. If the object does
 * not have an "id" field it will throw an exception
 */
fun <E : RealmModel?> getNextId(
    realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration,
    clazz: Class<E>
): Int {
    return getMaxId(
        realmConfiguration,
        clazz
    ) + 1
}

/**
 * Finds the maximum value of the "id" +1. If the object does
 * not have an "id" field it will throw an exception
 */
fun <E : RealmModel?> getNextIdWithoutClosing(
    realm: Realm,
    clazz: Class<E>
): Int {
    return getMaxIdWithoutClosing(
        realm = realm,
        clazz = clazz
    ) + 1
}