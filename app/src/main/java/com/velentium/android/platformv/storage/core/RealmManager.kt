package com.velentium.android.platformv.storage.core

import android.content.Context
import com.velentium.android.platformv.storage.core.utils.updateWithoutClosing
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmQuery

@Suppress("MemberVisibilityCanBePrivate")
open class RealmManager {

    //region Public Methods

    @Suppress("MemberVisibilityCanBePrivate")
    open fun resetRealm(realmConfiguration: RealmConfiguration = defaultConfiguration) {
        Realm
            .getInstance(realmConfiguration)
            .apply {
                beginTransaction()
                deleteAll()
                commitTransaction()
                close()
            }
    }

    open fun initialize(
        context: Context,
        schemaVersion: Long = DEFAULT_SCHEMA_VERSION,
        deleteIfMigrationNeeded: Boolean = true
    ) {
        Realm.init(context)
        Realm.setDefaultConfiguration(
            getConfiguration(
                schemaVersion = schemaVersion,
                deleteIfMigrationNeeded = deleteIfMigrationNeeded
            )
        )
    }

    open fun getDefault(context: Context): Realm {
        return try {
            Realm.getInstance(defaultConfiguration)
        } catch (error: IllegalStateException) {
            initialize(context = context)
            Realm.getInstance(defaultConfiguration)
        }
    }

    open fun <T, R : RealmModel> update(
        realm: Realm = default,
        clazz: T,
        updateBlock: R.() -> Unit
    ): Boolean where T : Class<R> {
        return realm
            .let {
                it.where(clazz)
                    .findFirst()
                    ?.updateWithoutClosing(realm = it, block = updateBlock) != null
            }
            .also { realm.close() }
    }

    open fun <T, R : RealmModel> update(
        realm: Realm = default,
        clazz: T,
        query: RealmQuery<R>.() -> RealmQuery<R>,
        updateBlock: R.() -> Unit
    ): Boolean where T : Class<R> {
        return realm
            .let {
                with(it.where(clazz)) { this.query() }
                    .findFirst()
                    ?.updateWithoutClosing(realm = it, block = updateBlock) != null
            }
            .also { realm.close() }
    }

    //endregion

    //region Companion

    companion object {
        const val DEFAULT_SCHEMA_VERSION: Long = 1L

        @JvmStatic
        val default: Realm
            get() = Realm.getInstance(defaultConfiguration)

        @JvmStatic
        val defaultConfiguration: RealmConfiguration
            get() = getConfiguration(deleteIfMigrationNeeded = true)
                .also {
                    Realm.setDefaultConfiguration(it)
                }

        @JvmStatic
        fun getConfiguration(
            schemaVersion: Long = DEFAULT_SCHEMA_VERSION,
            deleteIfMigrationNeeded: Boolean
        ): RealmConfiguration {
            return RealmConfiguration.Builder()
                .schemaVersion(schemaVersion)
                .apply {
                    if (deleteIfMigrationNeeded) {
                        deleteRealmIfMigrationNeeded()
                    }
                    allowQueriesOnUiThread(true)
                    // TODO: need to get all writes on BG thread
                    allowWritesOnUiThread(true)
                }
                .build()
        }
    }

    //endregion
}