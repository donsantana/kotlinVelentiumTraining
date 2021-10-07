package com.velentium.android.platformv.utils.rx

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Class that allows for queueing requests in the form of [Single]<[T]>. Requests then
 * are throttled if they are queued up in the timeframe of [timeout] * [timeUnit], calculated on
 * [scheduler].
 */
class ThrottleProcessor<T>(
    private val timeout: Long = 500,
    private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    private val scheduler: Scheduler = Schedulers.computation()
) {
    private val requestProcessor: BehaviorProcessor<Single<out T>> = BehaviorProcessor.create()

    private val resultProcessor: PublishProcessor<T> = PublishProcessor.create()

    init {
        requestProcessor
            .throttleWithTimeout(timeout, timeUnit, scheduler)
            .flatMapSingle { it }
            .subscribe(resultProcessor)
    }

    /**
     * Enqueue request in the form of [Single]<[T]>.
     *
     * Each request will wait for [timeout] * [timeUnit] for any other similar requests to be
     * enqueued. To ensure that this request will not be forever waiting due to new requests coming
     * in constantly within the [timeout] * [timeUnit] timeframe, the request will execute if the
     * wait is longer than [reqTimeout] * [reqTimeoutUnit].
     *
     * For example:
     *
     *  0. [timeout] is 3 [timeUnit] is [TimeUnit.SECONDS], [reqTimeout] is 4 and [reqTimeoutUnit]
     *  is [TimeUnit.SECONDS]
     *  1. For Consumer A, Request 1 is enqueued at Time 0
     *  2. 1 second passes
     *  3. For Consumer B, Request 2 is enqueued at Time 2
     *  4. 1 second passes
     *  5. For Consumer C, Request 3 is enqueued at Time 3
     *  6. 2 seconds pass
     *  7. For Consumer A, Request 1 is executed because 4 seconds has passed.
     *  8. 1 second passes
     *  9. For Consumer B & C, Request 3 is executed since no new requests came in before 3 seconds.
     */
    fun enqueueRequest(
        request: Single<out T>,
        reqTimeout: Long = 5,
        reqTimeoutUnit: TimeUnit = TimeUnit.SECONDS,
        reqTimeoutScheduler: Scheduler = Schedulers.computation()
    ): Single<T> {
        return resultProcessor
            .firstOrError()
            .doOnSubscribe { requestProcessor.onNext(request) }
            .timeout(reqTimeout, reqTimeoutUnit, reqTimeoutScheduler, request)
    }
}