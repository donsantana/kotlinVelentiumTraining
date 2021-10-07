package com.velentium.android.platformv.uicontrols.common.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment

/**
 * Abstract class that implements the logic needed to have a full-screen
 * dialog fragment.  Child class should derive and call the parents lifecycle methods.
 */
abstract class FullScreenDialogFragment : DialogFragment() {

    //region Properties

    companion object {
        val TAG: String = FullScreenDialogFragment::class.java.simpleName
        @DrawableRes var FULL_SCREEN_BACKGROUND = android.R.color.transparent
    }

    protected lateinit var fullScreenDialog: Dialog

    /**
     * handler provided for all derived classes, used in HUD methods
     */
    protected val handler = Handler(Looper.getMainLooper())

    //endregion

    //region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context ?: run {
            // should never happen
            throw AssertionError("Failed to create full screen dialog, missing context.")
        }
        fullScreenDialog = Dialog(context)
        fullScreenDialog.window?.apply {
            // ditch the title bar from the dialog fragment
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(FULL_SCREEN_BACKGROUND)
        }
        return fullScreenDialog

        // in derived class, call this as: super.onCreateDialog(savedInstanceState)
        // but do not assign it's value to anything, although if you do
        // fullScreenDialog = super.onCreateDialog(savedInstanceState)
        // you are just basically saying: fullScreenDialog = fullScreenDialog

        // after calling super.onCreateDialog(savedInstanceState), derived classes should
        // set the content view, etc., as in the sample code:
        //        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //            isCancelable = false
        //
        //            // super call initializes fullScreenDialog
        //            super.onCreateDialog(savedInstanceState) // <-- This is calling the code above
        //
        //            // load the layout
        //            fullScreenDialog.setContentView(R.layout.dialog_fragment_heads_up_display)
        //
        //            // proceed to get refs. to various controls within the layout
        //            titleTextView = fullScreenDialog.findViewById(R.id.hudTitleTextView)
        //            spinnerView = fullScreenDialog.findViewById(R.id.hudCircularProgressBar)
        //
        //            // returning this super.fullScreenDialog, from derived class
        //            return fullScreenDialog
        //        }
    }

    override fun onStart() {
        super.onStart()
        if (::fullScreenDialog.isInitialized) {
            fullScreenDialog.window?.apply {
                // make the dialog fragment match the window size
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        } else {
            val msg = "fullScreenDialog must be initialized by child class"
            Log.e(TAG, msg)
            throw AssertionError(msg)
        }
    }

    //endregion

    //region Public Methods

    /**
     * Shows the keyboard
     * @param view - this should be an EditText
     */
    fun showKeyboard(view: View) {
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imManager ->
            imManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /**
     * Hides the keyboard
     * @param view - this should be an EditText that you want the keyboard hidden for
     */
    fun hideKeyboard(view: View) {
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { imManager ->
            imManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    //endregion
}