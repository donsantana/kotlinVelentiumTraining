package com.velentium.android.platformv.utils.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.SoundEffectConstants
import android.view.View

fun View.hapticClick() {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (vibrator.hasVibrator()) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator.vibrate(effect)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val effect = VibrationEffect.createOneShot(100L, 100)
                vibrator.vibrate(effect)
            }
            else -> {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100L)
            }
        }
    } else {
        playSoundEffect(SoundEffectConstants.CLICK)
    }
}