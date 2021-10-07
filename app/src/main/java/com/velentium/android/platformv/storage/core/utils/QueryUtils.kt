package com.velentium.android.platformv.storage.core.utils

import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmQuery
import io.realm.RealmResults


inline fun <reified T : RealmModel> RealmQuery<T>.isNull(
    doCheck: Boolean,
    fieldName: String
): RealmQuery<T> {
    return if (doCheck) {
        this.isNull(fieldName)
    } else {
        this
    }
}

inline fun <reified T : RealmModel> RealmQuery<T>.isNotNull(
    doCheck: Boolean,
    fieldName: String
): RealmQuery<T> {
    return if (doCheck) {
        this.isNotNull(fieldName)
    } else {
        this
    }
}

inline fun <reified E : RealmModel> getFirstByUuid(realm: Realm, uuid: String): E? {
    return realm.where(E::class.java)
        .equalTo("uuid", uuid)
        .findFirst()
}