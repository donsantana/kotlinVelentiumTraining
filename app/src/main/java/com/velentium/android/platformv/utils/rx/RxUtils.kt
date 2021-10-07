package com.velentium.android.platformv.utils.rx

import android.util.Log
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

//region Subscription Related

/**
 * Subscription that handles the logging of error and complete actions.
 * Use when you only need to define the onNext action
 */
fun <T> Flowable<T>.simpleSubscribe(tag: String = "RxUtils", action: (T) -> Unit): Disposable {
    val onNext = Consumer<T> { t: T -> action.invoke(t) }
    val onError: Consumer<in Throwable> = Consumer {
        Log.e(tag, "simpleSubscribe: ", it)
    }

    val onComplete = Action {
        Log.d(tag, "simpleSubscribe: Completed!")
    }
    return subscribe(onNext, onError, onComplete)
}

/**
 * Subscription that handles the logging of error action.
 * Use when you only need to define the onSuccess action
 */
fun <T> Single<T>.simpleSubscribe(tag: String = "RxUtils", action: (T) -> Unit): Disposable {
    val onNext = Consumer<T> { t: T -> action.invoke(t) }
    val onError: Consumer<in Throwable> = Consumer {
        Log.e(tag, "simpleSubscribe: ", it)
    }
    return subscribe(onNext, onError)
}

/**
 * Subscription that handles the logging of error action.
 * Use when you only need to define the onSuccess action
 */
fun Completable.simpleSubscribe(tag: String = "RxUtils", action: () -> Unit): Disposable {
    val onComplete = Action { action.invoke() }
    val onError: Consumer<in Throwable> = Consumer {
        Log.e(tag, "simpleSubscribe: ", it)
    }
    return subscribe(onComplete, onError)
}

//endregion

//region Do operators

fun <T> Flowable<T>.doOnNextOrError(action: () -> Unit): Flowable<T> {
    return this
        .doOnNext { action.invoke() }
        .doOnError { action.invoke() }
}

/**
 * log the call backs for [Single].
 */
fun <T> Single<T>.logCallbacks(
    classTag: String,
    functionTag: String,
    vararg additionalTags: String
): Single<T> {
    val tag = "logCallbacks: $functionTag:${
        additionalTags.joinToString(
            separator = ": ",
            prefix = " ",
            postfix = ":"
        )
    }"
    return this
        .doOnSubscribe { Log.d(classTag, "$tag onSubscribe") }
        .doOnSuccess { Log.d(classTag, "$tag onSuccess: $it") }
        .doOnError { Log.d(classTag, "$tag onError: $it") }
        .doOnDispose { Log.d(classTag, "$tag onDispose") }
}

/**
 * log the call backs for [Flowable].
 */
fun <T> Flowable<T>.logCallbacks(
    classTag: String,
    functionTag: String,
    vararg additionalTags: String
): Flowable<T> {
    val tag = "logCallbacks: $functionTag:${
        additionalTags.joinToString(
            separator = ": ",
            prefix = " ",
            postfix = ":"
        )
    }"
    return this
        .doOnSubscribe { Log.d(classTag, "$tag onSubscribe") }
        .doOnComplete { Log.d(classTag, "$tag onComplete") }
        .doOnNext { Log.d(classTag, "$tag onNext: $it") }
        .doOnCancel { Log.d(classTag, "$tag onCancel") }
        .doOnError { Log.d(classTag, "$tag onError: $it") }
        .doOnTerminate { Log.d(classTag, "$tag onTerminate") }
}

/**
 * log the call backs for [Completable]
 */

fun Completable.logCallbacks(
    classTag: String,
    functionTag: String,
    vararg additionalTags: String
): Completable {
    val tag = "logCallbacks: $functionTag:${
        additionalTags.joinToString(
            separator = ": ",
            prefix = " ",
            postfix = ":"
        )
    }"
    return this
        .doOnSubscribe { Log.d(classTag, "$tag onSubscribe: ") }
        .doOnTerminate { Log.d(classTag, "$tag onTerminate: ") }
        .doOnDispose { Log.d(classTag, "$tag onDispose: ") }
        .doOnEvent { Log.d(classTag, "$tag onEvent: ") }
        .doOnComplete { Log.d(classTag, "$tag onComplete: ") }
        .doOnError { Log.e(classTag, "$tag onError: ", it) }
}

//endregion

//region Conditional Operators

/**
 * If [ifClause] with the emitted item is true, emits item, otherwise throws [error].
 * [error] defaults to [Exception].
 */
fun <T : Any, W : Throwable> Single<T>.onlyIf(
    error: W? = null,
    ifClause: (T) -> Boolean
): Single<T> {
    return this.map {
        if (ifClause(it)) {
            it
        } else {
            throw error ?: Exception()
        }
    }
}

/**
 * Overloading for [Flowable.timeout]. Instead of checking timeout for every emission, it only
 * times out if the first emission is not received by [timeout]*[timeUnit]
 */
fun <T> Flowable<T>.timeoutForFirst(
    timeout: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation()
): Flowable<T> {
    return this.ambWith(
        Flowable.error<T>(TimeoutException())
            .delay(
                timeout,
                timeUnit,
                scheduler,
                true
            )
    )
}

/**
 * Overloading for [Single.timeout]. Instead of checking timeout for every emission, it only
 * times out if the first emission is not received by [timeout]*[timeUnit]
 */
fun <T> Single<T>.timeoutForFirst(
    timeout: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation()
): Single<T> {
    return this.ambWith {
        Single.error<T>(TimeoutException())
            .delay(
                timeout,
                timeUnit,
                scheduler,
                true
            )
    }
}

/**
 * Overloading for [Maybe.timeout]. Instead of checking timeout for every emission, it only
 * times out if the first emission is not received by [timeout]*[timeUnit]
 */
fun <T> Maybe<T>.timeoutForFirst(
    timeout: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation()
): Maybe<T> {
    return this.ambWith {
        Single.error<T>(TimeoutException())
            .delay(
                timeout,
                timeUnit,
                scheduler,
                true
            )
    }
}


/**
 * Overloading for [Maybe.timeout]. Instead of checking timeout for every emission, it only
 * times out if the first emission is not received by [timeout]*[timeUnit]
 */
fun Completable.timeoutForFirst(
    timeout: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation()
): Completable {
    return this.ambWith {
        Completable.error(TimeoutException())
            .delay(
                timeout,
                timeUnit,
                scheduler,
                true
            )
    }
}

//endregion

//region Map/FlatMap related

/**
 * Convenience function that executes [mapBlock] if [ifClause] returns true.
 * Otherwise it throws [error], Exception is default for [error].
 */
fun <T : Any, R : Any, W : Throwable> Single<T>.mapIf(
    ifClause: (T) -> Boolean,
    mapBlock: (T) -> R,
    error: W? = null
): Single<R> {
    return this.map {
        if (ifClause(it)) {
            mapBlock(it)
        } else {
            throw error ?: Exception()
        }
    }
}

/**
 * Convenience function that returns [flatMapSingle] if [ifClause] returns true.
 * Otherwise it throws [error], Exception is default for [error].
 */
fun <T : Any, R : Any, W : Throwable> Single<T>.flatMapIf(
    ifClause: (T) -> Boolean,
    flatMapSingle: Single<R>,
    error: W? = null
): Single<R> {
    return this.flatMap {
        if (ifClause(it)) {
            flatMapSingle
        } else {
            throw error ?: Exception()
        }
    }
}

/**
 * Convenience function that invokes the map function, and if and only if the resulting value
 * is not null emits it.
 */
fun <T : Any, R : Any> Flowable<R>.mapIfNotNull(mapBlock: (R) -> T?): Flowable<T> {
    return this.map { Optional.ofNullable(mapBlock.invoke(it)) }
        .filter { it.isPresent }
        .map { it.get() }
}

/**
 * Convenience function that invokes the map function, and if and only if the resulting value
 * is not null emits it.
 */
fun <T : Any, R : Any, W : Throwable> Single<R>.mapIfNotNull(
    error: W,
    mapBlock: (R) -> T?
): Single<T> {
    return this.map { Optional.ofNullable(mapBlock.invoke(it)) }
        .onlyIf(error = error) { it.isPresent }
        .map { it.get() }
}

/**
 * Convenience function that invokes the map function, and if and only if the resulting value
 * is not null emits it.
 */
fun <T : Any, R : Any> Single<R>.mapIfNotNull(mapBlock: (R) -> T?): Single<T> {
    return this.map { Optional.ofNullable(mapBlock.invoke(it)) }
        .onlyIf(error = NullPointerException()) { it.isPresent }
        .map { it.get() }
}

/**
 * Convenience function that invokes the [block] function then returns the value.
 * Meant for usage with functions that have void results. (e.g. [Array.sort])
 */
fun <T : Any> Flowable<T>.applyMap(block: (T) -> Unit): Flowable<T> {
    return this.map {
        block.invoke(it)
        it
    }
}

/**
 * Convenience function that invokes the [block] function then returns the value.
 * Meant for usage with functions that have void results. (e.g. [Array.sort])
 */
fun <T : Any> Single<T>.applyMap(block: (T) -> Unit): Single<T> {
    return this.map {
        block.invoke(it)
        it
    }
}
//endregion

//region Collect/Reduce related

/**
 * Convenience function that simply converts a [Flowable]<[T]> into a [Single<[T]>] by collecting
 * all the emissions of [Flowable]<[T]>.
 */
fun <T> Flowable<T>.collectIntoMutablList(): Single<MutableList<T>> {
    return this.collectInto(mutableListOf(), { seed, newItem -> seed.add(newItem) })
}

//endregion

//region Composite Disposable related

fun CompositeDisposable.safeDispose() {
    if (!this.isDisposed) {
        this.dispose()
    }
}
//endregion

//region Conversion

fun <T : Any, W : Any> Single<T>.asNever(): Flowable<W> {
    return this.flatMapPublisher { Flowable.never() }
}

fun <T : Any, W : Any> Flowable<T>.asNever(): Flowable<W> {
    return this.flatMap { Flowable.never() }
}

//endregion