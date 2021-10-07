package com.velentium.android.platformv.utils.ui

import android.view.SoundEffectConstants
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.velentium.android.platformv.R


var Fragment.navigationBarTitle: String
    get() = (activity as? AppCompatActivity)?.supportActionBar?.title?.toString() ?: ""
    set(value) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = value
    }

fun Fragment.showDismissSnackBar(
    message: String?,
    @StringRes dismissResId: Int = R.string.dismiss,
    length: Int = Snackbar.LENGTH_LONG,
    onDismiss: (() -> Unit)? = null
) {
    val snack = Snackbar.make(requireView(), message ?: getString(dismissResId), length)
    snack.setAction(dismissResId) {
        it.playSoundEffect(SoundEffectConstants.CLICK)
        snack.dismiss()
    }
    snack.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
            onDismiss?.invoke()
            super.onDismissed(transientBottomBar, event)
        }
    })
    snack.show()
}