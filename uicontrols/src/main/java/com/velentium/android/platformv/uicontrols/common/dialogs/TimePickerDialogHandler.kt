package com.velentium.android.platformv.uicontrols.common.dialogs

import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.velentium.android.platformv.uicontrols.R
import kotlin.math.ceil

/**
 * Wraps the logic of getting the time using the [TimePickerDialog], handling the dialog lifecycle,
 * and having the option for time snap-to, a certain minute interval.
 */
object TimePickerDialogHandler {

    const val TAG = "TimePickerDialogHandler"
    private var timePickerDialog: TimePickerDialog? = null
    private var snapToMinute = 0

    /**
     * Must be set in order to receive time callback information, from the [TimePickerDialog]
     */
    var timePickerDialogListener: TimePickerDialog.OnTimeSetListener? = null

    /**
     * Adjust the time by [snapToMinute] as needed, then calls the passed in [timePickerDialogListener],
     * and dismisses the dialog.
     */
    private val internalTimePickerDialogListener =
        TimePickerDialog.OnTimeSetListener { picker, hour, minute ->
            Log.v(TAG, "Time chosen is: ${"%02d:%02d".format(hour, minute)}")

            if (snapToMinute > 1 && minute != 0 && minute % snapToMinute != 0) {
                picker.minute =
                    (ceil(minute.toDouble() / snapToMinute) * snapToMinute).toInt().takeIf { it < 60 }
                        ?: run {
                            picker.hour += 1
                            0
                        }
            }
//            if (BuildConfig.DEBUG) {
//                val hrs =
//                    if (picker.hour > 12) picker.hour - 12 else if (picker.hour == 0) 12 else picker.hour
//                val meridian = if (picker.hour >= 12) "pm" else "am"
//                val time = "%d:%02d %s".format(hrs, picker.minute, meridian)
//                Log.v(TAG, "Adjusted time is: $time")
//            }

            timePickerDialogListener?.onTimeSet(picker, picker.hour, picker.minute)
            dismissTimePickerDialog()
        }

    /**
     * Shows a TimePickerDialog, with a customized theme
     *
     * @param context
     * @param startHour
     * @param startMinute
     * @param snapToMinute
     * @param is24HourClock
     * @param isCancellable
     * @param positiveActionResId
     * @param negativeActionResId
     * @param listener
     */
    fun showTimePickerDialog(
        context: Context,
        startHour: Int,
        startMinute: Int,
        snapToMinute: Int = 0,
        is24HourClock: Boolean = false,
        isCancellable: Boolean = false,
        @StringRes positiveActionResId: Int = android.R.string.ok,
        @StringRes negativeActionResId: Int = android.R.string.cancel,
        @StyleRes themeResId: Int = R.style.PlatformTimePickerDialog,    // TODO: figure out why this styling isn't working
        listener: TimePickerDialog.OnTimeSetListener
    ) {
        TimePickerDialogHandler.snapToMinute = snapToMinute
        timePickerDialogListener = listener
        timePickerDialog = TimePickerDialog(
            context,
            themeResId,
            internalTimePickerDialogListener,
            startHour,
            startMinute,
            is24HourClock
        ).apply {
            setCancelable(isCancellable)
        }
        timePickerDialog!!.setOnShowListener { d ->
            val positiveButton = (d as TimePickerDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = d.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveButton?.contentDescription =
                context.resources.getResourceEntryName(positiveActionResId)
            negativeButton
                ?.apply {
                    contentDescription = context.resources.getResourceEntryName(negativeActionResId)
                }
                ?.setOnClickListener {
                    dismissTimePickerDialog()
                }
        }
        timePickerDialog!!.show()
    }

    /**
     * Should be called to dismiss this dialog
     */
    fun dismissTimePickerDialog() {
        timePickerDialog?.let {
            if (it.isShowing) it.dismiss()
        }
        timePickerDialog = null
    }
}