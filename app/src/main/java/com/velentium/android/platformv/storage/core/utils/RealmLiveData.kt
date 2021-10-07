package com.velentium.android.platformv.storage.core.utils

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults


class RealmLiveData<T : RealmModel>(private val results: RealmResults<T>) :
    LiveData<RealmResults<T>>() {
    init {
        this.value = results
    }

    private val listener = RealmChangeListener<RealmResults<T>> { results ->
        value = results
    }

    override fun onActive() {
        this.value = results
        results.addChangeListener(listener)
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }
}