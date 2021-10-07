package com.velentium.android.platformv.uicontrols.common.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.FragmentManager
import com.velentium.android.platformv.uicontrols.R

/**
 * This is a callable dialog fragment, that can be called with an optional title, and
 * will display an android spinner, to indicate background processing.
 */
class HUDDialogFragment : FullScreenDialogFragment() {

    //region Properties

    private lateinit var titleTextView: AppCompatTextView
    private lateinit var spinnerView: ContentLoadingProgressBar

    private var titleText = ""

    private var timeoutAfterMillis: Long? = null

    private val timeoutRunnable = Runnable {
        Log.w(logTag, "$logTag dismissed via failsafe timeout.")
        dismiss()
    }

    companion object {
        val logTag: String = HUDDialogFragment::class.java.simpleName

        /**
         * Default timeout 10 seconds
         */
        private const val DEFAULT_TIMEOUT_MS = 10L * 1000L

        /**
         * Min time it usually takes for anim. to happen 1.5x
         * adjust as needed
         */
        const val MIN_TIMEOUT_MS = 1200L

        const val TIMEOUT_KEY = "hud_timeout"
        const val TITLE_KEY = "hud_title"

        /**
         * "Static" callback, since only 1 HUD will ever be shown at a time.
         */
        var dismissCallback: (() -> Unit)? = null

        /**
         * @return Returns a new instance of HUDDialogFragment
         */
        fun newInstance(
            title: String? = null,
            timeoutMillis: Long? = DEFAULT_TIMEOUT_MS
        ): HUDDialogFragment {
            val hud = HUDDialogFragment()
            val args = Bundle()
            args.putString(TITLE_KEY, title ?: "")
            timeoutMillis?.let { millis ->
                args.putLong(TIMEOUT_KEY, millis)
            }
            hud.arguments = args
            return hud
        }
    }

    //endregion

    //region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handle the arguments passed with title, and timeout if any
        arguments?.takeIf { it.containsKey(TITLE_KEY) || it.containsKey(TIMEOUT_KEY) }
            ?.let { bundle ->
                if (bundle.containsKey(TIMEOUT_KEY)) {
                    timeoutAfterMillis = bundle.getLong(TIMEOUT_KEY, -1).takeIf { it > 0 }
                    val millis = timeoutAfterMillis ?: 0
                    if (millis in 1 until MIN_TIMEOUT_MS) {
                        // needed to ensure minimum time to avoid flickering issues
                        timeoutAfterMillis = MIN_TIMEOUT_MS
                    }
                }

                if (bundle.containsKey(TITLE_KEY)) {
                    titleText = bundle.getString(TITLE_KEY, "")
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // super call initializes fullScreenDialog
        super.onCreateDialog(savedInstanceState)

        isCancelable = false

        // load the layout
        fullScreenDialog.setContentView(R.layout.dialog_fragment_heads_up_display)

        // get the title
        titleTextView = fullScreenDialog.findViewById(R.id.hudTitleTextView)

        // get the ContentLoadingProgressBar
        spinnerView = fullScreenDialog.findViewById(R.id.hudCircularProgressBar)

        return fullScreenDialog
    }

    override fun onStart() {
        super.onStart()

        // set the title to null if blank or missing
        titleTextView.text = titleText.takeIf { it.isNotBlank() }
        titleTextView.visibility = View.VISIBLE

        // setup a timeout callback if needed
        timeoutAfterMillis?.let { timeout ->
            Log.v(logTag, "HUD Dialog shown, with timeout $timeout")
            handler.postDelayed(timeoutRunnable, timeout)
        } ?: run {
            Log.v(logTag, "HUD Dialog shown, with no timeout")
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.v(logTag, "HUD Dialog dismissed")

        // remove any lingering timeouts since its closing
        handler.removeCallbacks(timeoutRunnable)

        super.onDismiss(dialog)

        if (dismissCallback == null) {
            Log.v(logTag,"Dismiss already called")
            return
        }

        // else
        synchronized(this) {
            dismissCallback?.invoke()
            dismissCallback = null
        }
    }

    //endregion


    //region Public Methods

    /**
     * This method should be called instead of show, if you need a callback, for dismiss.
     * FragmentManager.beginTransaction SHOULD NOT be called, as this method calls it.
     *
     * @param manager - This is required to be passed so it can be used from other dialog fragments
     * @param onDismissed - Callback for when the HUD is dismissed, leave null to ignore
     */
    fun showHUD(manager: FragmentManager, onDismissed: (() -> Unit)? = null) {
        dismissCallback = onDismissed
        Log.v(logTag, "HUD was dismissed via showHUD")
        manager.beginTransaction()
        show(manager, logTag)
    }

    /**
     * This method should be called instead of dismiss
     */
    fun hideHUD() {
        Log.v(logTag, "HUD was dismissed via hideHUD")
        dismiss()
    }

    //endregion
}