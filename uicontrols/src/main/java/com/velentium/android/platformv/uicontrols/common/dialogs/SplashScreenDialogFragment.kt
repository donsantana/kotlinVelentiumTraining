package com.velentium.android.platformv.uicontrols.common.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.FragmentManager
import com.velentium.android.platformv.uicontrols.R

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SplashScreenDialogFragment: FullScreenDialogFragment() {

    private lateinit var layout: ConstraintLayout
    private lateinit var logoImage: ImageView
    private lateinit var spinnerView: ContentLoadingProgressBar
    private var hideAfterMillis: Long? = null

    private val hideRunnable = Runnable {
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        isCancelable = false

        fullScreenDialog.setContentView(R.layout.dialog_fragment_splash_screen)
        layout = fullScreenDialog.findViewById(R.id.splash_layout)
        logoImage = fullScreenDialog.findViewById(R.id.splash_logo_image)
        spinnerView = fullScreenDialog.findViewById(R.id.splashSpinner)

        arguments?.let { bundle ->
            if (bundle.containsKey(ARG_BACKGROUND_RES_ID)) {
                layout.setBackgroundResource(bundle.getInt(ARG_BACKGROUND_RES_ID))
            }
            if (bundle.containsKey(ARG_LOGO_RES_ID)) {
                logoImage.setImageResource(bundle.getInt(ARG_LOGO_RES_ID))
            }
            if (bundle.containsKey(ARG_HAS_SPINNER)) {
                spinnerView.isVisible = bundle.getBoolean(ARG_HAS_SPINNER)

                if (bundle.containsKey(ARG_SPINNER_TINT_RES_ID)) {
                    spinnerView.indeterminateTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            bundle.getInt(ARG_SPINNER_TINT_RES_ID)
                        )
                    )
                }
                if (spinnerView.isVisible) {
                    spinnerView.animate()
                }
            }
        }

        return fullScreenDialog
    }

    override fun onStart() {
        super.onStart()
        hideAfterMillis?.let {
            handler.postDelayed(hideRunnable, it)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        handler.removeCallbacks(hideRunnable)
        synchronized(this) {
            dismissCallback?.invoke()
            dismissCallback = null
        }
    }

    fun showSplashScreen(manager: FragmentManager,
                         hideAfterMilliseconds: Long? = null,
                         onDismissed: (() -> Unit)? = null) {
        dismissCallback = onDismissed
        hideAfterMillis = hideAfterMilliseconds
        manager.beginTransaction()
        show(manager, TAG)
    }

    companion object {
        val TAG: String = SplashScreenDialogFragment::class.java.simpleName

        const val ARG_LOGO_RES_ID: String = "ARG_LOGO_RES_ID"
        const val ARG_BACKGROUND_RES_ID: String = "ARG_BACKGROUND_RES_ID"
        const val ARG_HAS_SPINNER: String = "ARG_HAS_SPINNER"
        const val ARG_SPINNER_TINT_RES_ID: String = "ARG_SPINNER_TINT_RES_ID"

        /**
         * "Static" callback, since only 1 HUD will ever be shown at a time.
         */
        @JvmStatic
        var dismissCallback: (() -> Unit)? = null

        @JvmStatic
        fun newInstance(
            @DrawableRes logoImageResId: Int,
            @ColorRes backgroundColorResId: Int,
            @ColorRes spinnerTintColorResId: Int,
            hasSpinner: Boolean = true
        ): SplashScreenDialogFragment {
            val splashDialog = SplashScreenDialogFragment()
            splashDialog.arguments = bundleOf(
                ARG_LOGO_RES_ID to logoImageResId,
                ARG_BACKGROUND_RES_ID to backgroundColorResId,
                ARG_SPINNER_TINT_RES_ID to spinnerTintColorResId,
                ARG_HAS_SPINNER to hasSpinner
            )
            return splashDialog
        }
    }
}