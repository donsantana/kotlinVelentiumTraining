package com.velentium.android.platformv.storage.core.utils

import com.velentium.android.platformv.storage.core.RealmManager
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel

/**
 * Saves the object to realm by creating it. For updates to existing objects use
 * [update].
 * This will throw an exception if you try to save and an object with the
 * same Primary Key exists.
 * @return the realm managed version of [this]
 */
fun <T : RealmModel> T.save(realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration): T {
    val saved: T
    val realm = Realm.getInstance(realmConfiguration)
    try {
        realm.beginTransaction()
        saved = realm.copyToRealm(this)
        realm.commitTransaction()
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        throw e
    } finally {
        realm.close()
    }
    return saved
}

/**
 * Saves the object to realm by creating it. For updates to existing objects use
 * [update].
 * This will throw an exception if you try to save and an object with the
 * same Primary Key exists.
 * @return the realm managed version of [this]
 */
fun <T : RealmModel> T.saveWithoutClosing(realm: Realm): T {
    val saved: T
    try {
        realm.beginTransaction()
        saved = realm.copyToRealm(this)
        realm.commitTransaction()
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        throw e
    }
    return saved
}

/**
 * Saves the object to realm by creating it. For updates to existing objects use
 * [update].
 * This will throw an exception if you try to save and an object with the
 * same Primary Key exists.
 * @return the realm managed version of [this]
 */
fun <T : RealmModel> MutableList<T>.saveWithoutClosing(
    realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration,
    realm: Realm = Realm.getInstance(
        realmConfiguration
    )
): MutableList<T> {
    val saved: MutableList<T> = mutableListOf()
    try {
        realm.beginTransaction()
        this.forEach {
            saved.add(realm.copyToRealm(it))
        }
        realm.commitTransaction()
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        throw e
    }
    return saved
}