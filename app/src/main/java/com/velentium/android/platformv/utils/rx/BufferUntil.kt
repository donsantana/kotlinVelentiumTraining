package com.velentium.android.platformv.utils.rx

import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import no.nordicsemi.android.ble.data.Data

fun Flowable<ByteArray>.bufferUntil(
    size: Int,
    exact: Boolean = true,
    compositeDisposable: CompositeDisposable
): Flowable<ByteArray> {
    fun split(data: ByteArray, boundary: Int): Pair<ByteArray, ByteArray> {
        if (data.count() < boundary) {
            return Pair(data, byteArrayOf())
        }
        val head = data.copyOfRange(0, boundary)
        val tail = data.copyOfRange(boundary, data.count())
        return Pair(head, tail)
    }

    var count = 0
    var buffer = byteArrayOf()

    return Observable.create<ByteArray> { observer ->
        compositeDisposable += this
            .doOnComplete {
                val (head, tail) = split(buffer, size)
                observer.onNext(head)
                if (tail.isNotEmpty()) {
                    observer.onNext(tail)
                }
                observer.onComplete()
            }
            .subscribe({ data ->
                synchronized(this) {
                    buffer += data
                    count += data.count()
                    Log.v("bufferUntil", "Received $count bytes of $size total bytes.")
                    val shouldFlushBuffer = count >= size
                    if (!shouldFlushBuffer) {
                        return@synchronized
                    } else {
                        count = 0
                    }
                    if (!exact) {
                        observer.onNext(buffer)
                        buffer = byteArrayOf()
                        return@synchronized
                    }

                    val (head, tail) = split(buffer, size)
                    observer.onNext(head)
                    buffer = tail
                    return@synchronized
                }
            }, { error ->
                observer.onError(error)
                buffer = byteArrayOf()
            })
    }.toFlowable(BackpressureStrategy.BUFFER)
}

fun Flowable<Data>.bufferData(
    size: Int,
    exact: Boolean = true,
    compositeDisposable: CompositeDisposable
): Flowable<Data> {
    return this
        .mapIfNotNull { it.value }
        .bufferUntil(
            size = size,
            exact = exact,
            compositeDisposable = compositeDisposable
        )
        .map { Data(it) }
}