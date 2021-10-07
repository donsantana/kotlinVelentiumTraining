package com.velentium.android.platformv.utils

import android.os.*
import android.util.Log

typealias PathChangedCallback = (PathMonitorEvent, String?) -> Unit

enum class PathMonitorEvent(val maskValue: Int) {
    ALL_EVENTS(maskValue = FileObserver.ALL_EVENTS),
    ATTRIB(maskValue = FileObserver.ATTRIB),
    CLOSE_NO_WRITE(maskValue = FileObserver.CLOSE_NOWRITE),
    CREATE(maskValue = FileObserver.CREATE),
    DELETE(maskValue = FileObserver.DELETE),
    DELETE_SELF(maskValue = FileObserver.DELETE_SELF),
    MODIFY(maskValue = FileObserver.MODIFY),
    MOVED_FROM(maskValue = FileObserver.MOVED_FROM),
    MOVED_TO(maskValue = FileObserver.MOVED_TO),
    MOVED_SELF(maskValue = FileObserver.MOVE_SELF),
    OPEN(maskValue = FileObserver.OPEN);
}

@Suppress("DEPRECATION", "unused")
abstract class PathWatcher constructor(
    path: String,
    monitorFor: Set<PathMonitorEvent> = setOf(PathMonitorEvent.CREATE)
) : FileObserver(path, eventSetToMask(monitorFor)) {
    abstract fun startMonitoring(callback: PathChangedCallback)
    abstract fun stopMonitoring()

    companion object {
        @JvmStatic
        fun eventSetToMask(events: Set<PathMonitorEvent>): Int {
            return if (events.contains(PathMonitorEvent.ALL_EVENTS)) {
                PathMonitorEvent.ALL_EVENTS.maskValue
            } else {
                val mask = events.fold(0) { acc, event ->
                    return@fold (acc or event.maskValue)
                }
                return mask
            }
        }
    }
}

/**
 * Implements a [PathWatcher] for watching a given path for changes.
 */
class PathMonitor constructor(
    path: String,
    monitorFor: Set<PathMonitorEvent>
) : PathWatcher(path, monitorFor) {

    private val callbackHandler = Handler(Looper.getMainLooper())
    private var eventCallback: PathChangedCallback? = null

    override fun startMonitoring(callback: PathChangedCallback) {
        eventCallback = callback
        startWatching()
    }

    override fun stopMonitoring() {
        stopWatching()
        eventCallback = null
    }

    override fun onEvent(event: Int, path: String?) {
        Log.i(TAG, "Event occurred: $event, for path: $path, on thread: ${Thread.currentThread().name}")
        val callback = eventCallback  ?: kotlin.run {
            Log.i(TAG, "Event called $event, with no callback.")
            return
        }
        val eventType = PathMonitorEvent.values().firstOrNull { it.maskValue == event }
            ?: kotlin.run {
                Log.w(TAG, "Unhandled PathMonitor event: $event")
                return
            }
        callbackHandler.post {
            callback.invoke(eventType, path)
        }
    }

    companion object {
        val TAG: String = PathMonitor::class.java.simpleName
    }
}