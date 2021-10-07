package com.velentium.android.platformv.storage.core.utils

import io.realm.RealmList

fun <T> Array<T>.toRealmList(): RealmList<T> {
    val tempList = RealmList<T>()
    tempList.addAll(this)
    return tempList
}

fun <T> Collection<T>.toRealmList(): RealmList<T> {
    val tempList = RealmList<T>()
    tempList.addAll(this)
    return tempList
}