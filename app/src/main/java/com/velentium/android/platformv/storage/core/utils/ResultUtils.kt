package com.velentium.android.platformv.storage.core.utils

import com.velentium.android.platformv.storage.core.RealmManager
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults

fun <E : RealmModel?, R> getAllModels(
    clazz: Class<E>,
    body: (RealmResults<E>) -> R
): R? {
    val realm = Realm.getInstance(RealmManager.defaultConfiguration)
    val results: RealmResults<E>?
    var output: R? = null
    try {
        results = realm.where(clazz)
            .findAll()
        output = body.invoke(results)
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        e.printStackTrace()
    } finally {
        realm.close()
        return output
    }
}

fun <T : RealmModel> RealmResults<T>.asLiveData() = RealmLiveData(
    this
)

inline fun <reified T : RealmModel> RealmResults<T>.lastFlowable(): Flowable<T> {
    return this.asFlowable()
        .filter { it.size > 0 }
        .map { it.toTypedArray() }
        .map { it.last() }
}