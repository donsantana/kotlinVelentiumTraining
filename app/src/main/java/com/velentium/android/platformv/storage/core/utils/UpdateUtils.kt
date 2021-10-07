package com.velentium.android.platformv.storage.core.utils

import com.velentium.android.platformv.storage.core.RealmManager
import io.realm.*

/**
 * Updates the given Realm Object.
 */
fun <T : RealmModel> T.update(
    realmConfiguration: RealmConfiguration = RealmManager.defaultConfiguration,
    block: T.() -> Unit
) {
    val realm = Realm.getInstance(realmConfiguration)
    try {
        realm.executeTransaction {
            block()
        }
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        e.printStackTrace()
        throw e
    } finally {
        realm.close()
    }
}

/**
 * Updates the given Realm Object.
 * Note: Closes [realm]
 */
fun <T : RealmModel> T.update(
    realm: Realm,
    block: T.() -> Unit
) {
    try {
        realm.executeTransaction {
            block()
        }
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        e.printStackTrace()
        throw e
    } finally {
        realm.close()
    }
}

inline fun <T : RealmModel> T.updateNoInline(
    realm: Realm,
    crossinline block: T.() -> Unit
) {
    try {
        realm.executeTransaction {
            block()
        }
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        e.printStackTrace()
        throw e
    } finally {
        realm.close()
    }
}

/**
 * Updates the given Realm Object.
 */
fun <T : RealmModel> T.updateWithoutClosing(realm: Realm, block: T.() -> Unit) {
    realm.unSafeUpdate {
        block()
    }
}

fun <T : RealmObject> T.updateWithoutClosing(block: T.() -> Unit): T {
    realm.unSafeUpdate {
        block()
    }
    return this
}

fun <T, R : RealmModel> T.update(updateBlock: R.() -> Unit): Boolean where T : Class<R> {
    return RealmManager.default
        .let {
            it.where(this)
                .findFirst()
                ?.update(realm = it, block = updateBlock) != null
        }
}

inline fun <reified R : RealmModel> update(noinline updateBlock: R.() -> Unit): Boolean {
    return RealmManager.default
        .let {
            it.where(R::class.java)
                .findFirst()
                ?.update(realm = it, block = updateBlock) != null
        }
}

inline fun <reified R : RealmModel> update(
    query: RealmQuery<R>.() -> RealmQuery<R>,
    crossinline updateBlock: R.() -> Unit
): Boolean {
    return RealmManager.default
        .let {
            with(it.where(R::class.java)) { this.query() }
                .findFirst()
                ?.updateNoInline(realm = it, block = updateBlock) != null
        }
}

/**
 * Updates a list of [RealmModel] objects with the same update function.
 */
fun <T : RealmModel> MutableList<T>.updateWithoutClosing(realm: Realm, block: T.() -> Unit) {
    try {
        realm.beginTransaction()
        this.forEach {
            block.invoke(it)
        }
        realm.commitTransaction()
    } catch (e: Throwable) {
        if (realm.isInTransaction) {
            realm.cancelTransaction()
        }
        throw e
    }
}

/**
 * Only call this if you know what you are doing...
 */
fun <T> Realm.unSafeUpdate(block: () -> T) {
    try {
        this.executeTransaction {
            block.invoke()
        }
    } catch (e: Throwable) {
        if (this.isInTransaction) {
            this.cancelTransaction()
        }
        e.printStackTrace()
        throw e
    }
}